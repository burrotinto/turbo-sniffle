package de.burrotinto.turboSniffle.cv;

import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.val;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TextDedection {
    public static final String ENGRESTRICT_BEST_INT = "engrestrict_best_int";
    public static final String ENG_BEST = "eng";
    private final float scoreThresh = 0.5f;
    private final float nmsThresh = 0.4f;
    private final Net net = Dnn.readNetFromTensorflow("data/frozen_east_text_detection.pb");
    private final List<String> outNames = new ArrayList<String>();

    private final Tesseract tesseract = new Tesseract();
    private final Tesseract tesseractNumbers = new Tesseract();

    public TextDedection(String language, int dpi) {
        initDNN();

        tesseract.setDatapath("data");

        tesseractNumbers.setDatapath("data");
        if (language != null && !language.equals("")) {
            tesseractNumbers.setLanguage(language);
        }
        tesseractNumbers.setOcrEngineMode(1);
        addOptions("user_defined_dpi", "" + dpi);
    }

    public TextDedection() {
        this(ENGRESTRICT_BEST_INT, 300);
    }

    private void initDNN() {
        outNames.add("feature_fusion/Conv_7/Sigmoid");
        outNames.add("feature_fusion/concat_3");
    }

    public void addOptions(String var, String x) {
        tesseractNumbers.setTessVariable(var, x);
    }

    public List<RotatedRect> getTextAreas(Mat src) {

        // input image
        Mat frame = src.clone();
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        Size siz = frame.size();
        int W = (int) (siz.width / 4); // width of the output geometry  / score maps
        int H = (int) (siz.height / 4); // height of those. the geometry has 4, vertically stacked maps, the score one 1

        Mat blob = Dnn.blobFromImage(frame, 1.0, siz, new Scalar(123.68, 116.78, 103.94), true, true);
        net.setInput(blob);
        List<Mat> outs = new ArrayList<>(2);
        net.forward(outs, outNames);


        // Decode predicted bounding boxes.
        Mat scores = outs.get(0).reshape(1, H);
        // My lord and savior : http://answers.opencv.org/question/175676/javaandroid-access-4-dim-mat-planes/
        Mat geometry = outs.get(1).reshape(1, 5 * H); // don't hardcode it !
        List<Float> confidencesList = new ArrayList<>();
        List<RotatedRect> boxesList = decode(scores, geometry, confidencesList, scoreThresh);

        // Apply non-maximum suppression procedure.
        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confidencesList));
        RotatedRect[] boxesArray = boxesList.toArray(new RotatedRect[0]);
        MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxesRotated(boxes, confidences, scoreThresh, nmsThresh, indices);

        // Render detections
        Point ratio = new Point((float) frame.cols() / siz.width, (float) frame.rows() / siz.height);
        int[] indexes = indices.toArray();
        List<RotatedRect> out = new ArrayList<>();
        for (int i = 0; i < indexes.length; ++i) {
            RotatedRect rot = boxesArray[indexes[i]];
            Point[] vertices = new Point[4];
            rot.points(vertices);
            for (int j = 0; j < 4; ++j) {
                vertices[j].x *= ratio.x;
                vertices[j].y *= ratio.y;
            }
            out.add(rot);
        }

        return out;
    }

    private static List<RotatedRect> decode(Mat scores, Mat geometry, List<Float> confidences, float scoreThresh) {
        // size of 1 geometry plane
        int W = geometry.cols();
        int H = geometry.rows() / 5;
        //System.out.println(geometry);
        //System.out.println(scores);

        List<RotatedRect> detections = new ArrayList<>();
        for (int y = 0; y < H; ++y) {
            Mat scoresData = scores.row(y);
            Mat x0Data = geometry.submat(0, H, 0, W).row(y);
            Mat x1Data = geometry.submat(H, 2 * H, 0, W).row(y);
            Mat x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y);
            Mat x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y);
            Mat anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y);

            for (int x = 0; x < W; ++x) {
                double score = scoresData.get(0, x)[0];
                if (score >= scoreThresh) {
                    double offsetX = x * 4.0;
                    double offsetY = y * 4.0;
                    double angle = anglesData.get(0, x)[0];
                    double cosA = Math.cos(angle);
                    double sinA = Math.sin(angle);
                    double x0 = x0Data.get(0, x)[0];
                    double x1 = x1Data.get(0, x)[0];
                    double x2 = x2Data.get(0, x)[0];
                    double x3 = x3Data.get(0, x)[0];
                    double h = x0 + x2;
                    double w = x1 + x3;
                    Point offset = new Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2);
                    Point p1 = new Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y);
                    Point p3 = new Point(-1 * cosA * w + offset.x, sinA * w + offset.y); // original trouble here !
                    RotatedRect r = new RotatedRect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h), -1 * angle * 180 / Math.PI);
                    detections.add(r);
                    confidences.add((float) score);
                }
            }
        }
        return detections;
    }

    @SneakyThrows
    @Synchronized
    String doOCRNumbers(BufferedImage sub) {
        return tesseractNumbers.doOCR(sub);
    }

    @SneakyThrows
    public List<RotatedRect> getTextAreasWithTess(Mat src) {
        List<RotatedRect> out = new ArrayList<>();
        try {
            val reg = tesseractNumbers.getSegmentedRegions(Helper.Mat2BufferedImage(src), ITessAPI.TessPageIteratorLevel.RIL_WORD);
            for (int i = 0; i < reg.size(); ++i) {
                Rectangle r = reg.get(i);
                RotatedRect rot = new RotatedRect(new Point(r.getCenterX(), r.getCenterY()), new Size(r.getWidth(), r.getHeight()), 0);
                out.add(rot);
            }
        } catch (Exception e) {

        }
        return out;

    }

    /**
     * Wie in https://github.com/tesseract-ocr/tessdoc/blob/e0a3ca5aff95b9e531ff56bb191a2ad5bcd188f6/images/borders.png
     * beschrieben wird ein 10 PX wei??er Rand eingef??gt
     *
     * @param sub
     * @return
     */
    @SneakyThrows
    public String doOCRNumbers(Mat sub) {
        Mat withBorders = new Mat(new Size(sub.width() + 20, sub.height() + 20), sub.type());
        withBorders.setTo(Helper.WHITE);

        sub.copyTo(withBorders.submat(10, sub.rows() + 10, 10, sub.cols() + 10));

//        Imgcodecs.imwrite("data/out/G1000_"+ System.currentTimeMillis()+ ".png", withBorders);
        return doOCRNumbers(Helper.Mat2BufferedImage(withBorders));
    }

    public Double doOCRBruteForceNumber(Mat org) {
        Mat sub = new Mat();
        Imgproc.resize(org, sub, new Size(org.size().width * 3, org.size().height * 3));
        Mat test = new Mat();

        HashMap<Double, AtomicInteger> results = new HashMap<>();
        for (int i = 50; i <= 210; i += 10) {
            Imgproc.threshold(sub, test, i, 255, Imgproc.THRESH_BINARY);
//            HighGui.imshow("", test);
//            HighGui.waitKey(100);
            val v = Helper.parseDoubleOrNAN(doOCRNumbers(Helper.sharpen(test)));
            if (!Double.isNaN(v)) {
                if (!results.containsKey(v)) {
                    results.put(v, new AtomicInteger());
                }
                results.get(v).incrementAndGet();
            }
        }

        val kandidat = results.entrySet()
                .stream().min((o1, o2) -> o2.getValue().get() - o1.getValue().get());
        if (kandidat.isPresent() && kandidat.get().getValue().get() >= 2) {
            return kandidat.get().getKey();
        } else {
            return Double.NaN;
        }

    }
}
//    @SneakyThrows
//    public String doOCR(BufferedImage sub) {
//        return tesseract.doOCR(sub);
//    }
//    @SneakyThrows
//    public String doOCR(Mat sub) {
//        return tesseract.doOCR(Helper.Mat2BufferedImage(sub));
//    }

