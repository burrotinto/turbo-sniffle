package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.TextDedection;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GarminG1000 {
    //Bei einem 12" Display ist die Länge 9,6" und die höhe 7,2" entsprich 106,666 DPI
    public static final Size SIZE = new Size(1024, 768);
    private static TextDedection textDedection = new TextDedection(TextDedection.ENGRESTRICT_BEST_INT, 107);
    private Mat g1000;


    private final static Rect altimeter = new Rect(new Point(700, 70), new Point(810, 450));

    @SneakyThrows
    public GarminG1000(Mat src) {
        g1000 = new Mat();
        Imgproc.resize(src, g1000, SIZE);
        textDedection.addOptions("tessedit_char_whitelist", "01234567890");

        g1000 = getBestThreshAndMat(g1000).p2;

//        int i = 50;
//        while (i <= SIZE.height || i <= SIZE.width) {
//            if (i <= SIZE.height) {
//                Imgproc.line(g1000, new Point(0, i), new Point(SIZE.width, i), Helper.BLACK);
//            }
//            if (i <= SIZE.width) {
//                Imgproc.line(g1000, new Point(i, 0), new Point(i, SIZE.height), Helper.BLACK);
//            }
//            i += 50;
//        }


//        val cannyEdgeDetector = GaugeFactory.getCanny();
//        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(g1000));
//        cannyEdgeDetector.process();
//
//        Map<RotatedRect, Mat> map = new HashMap<>();
//        ArrayList<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(cannyEdgeDetector.getEdgeMat(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        HighGui.imshow("canny", cannyEdgeDetector.getEdgeMat());
////        HighGui.waitKey();
//
//        for (int i = 0; i < contours.size(); i++) {
//            val rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
//
//            //Nur Konturen die eine Mindestgröße besitzen
//            if (rect.size.area() > 1000) {
////                Mat xxx = transformieren(g1000,rect,g1000.size());
//                try {
//                    HighGui.imshow(i + "", g1000.submat(rect.boundingRect()));
//                } catch (Exception e) {
//
//                }
//            }
//        }

        System.out.println(textDedection.doOCRNumbers(g1000.submat(altimeter)));


        HighGui.imshow("", g1000);
        HighGui.waitKey();
    }

    public static Pair<Integer, Mat> getBestThreshAndMat(Mat src) {
        Mat w = new Mat();
        int thresh = 200;
        int nmbrsCount = 0;
        boolean isReady = false;
//        List<Pair<Integer,Pair<Integer, Mat>>> all = new ArrayList<>();
//        for (int i = 100; i < 200; i+=5) {
//            Imgproc.threshold(src, w, thresh, 255, Imgproc.THRESH_BINARY);
//            Core.bitwise_not(w, w);
//            val numbers = textDedection.doOCRNumbers(w).split("\n");
//            all.add(new Pair<>(numbers.length,new Pair<>(thresh, w.clone())));
//
//        }
//        all.sort((o1, o2) -> o2.p1 - o1.p1);
//
//        return all.get(0).p2;

        Pair<Integer, Mat> out = null;
        do {
            Imgproc.threshold(src, w, thresh, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(w, w);
            val numbers = textDedection.doOCRNumbers(w).split("\n");
            System.out.println(numbers.length);
            if (nmbrsCount < numbers.length) {
                nmbrsCount = numbers.length;
                out = new Pair<>(thresh, w.clone());
            } else if (nmbrsCount > numbers.length) {
                isReady = true;
            }
            thresh -= 10;
        } while (!isReady);
        return out;

    }

    public static Mat transformieren(Mat mat, RotatedRect ellipse, Size size) {
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transformieren(mat, Arrays.stream(pts).collect(Collectors.toList()), size);
    }

    public static Mat transformieren(Mat mat, List<Point> corner, Size size) {

        List<Point> target = new ArrayList<Point>();
        target.add(new Point(0, 0));
        target.add(new Point(size.width, 0));
        target.add(new Point(size.width, size.height));
        target.add(new Point(0, size.height));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);

        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, size);

        return proj;
    }
}
