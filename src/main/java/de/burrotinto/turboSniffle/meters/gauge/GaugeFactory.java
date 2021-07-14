package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.ellipse.CannyEdgeDetector;
import de.burrotinto.turboSniffle.ellipse.EllipseDetector;
import de.burrotinto.turboSniffle.meters.gauge.impl.ScaleMarkExtraction;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



public class GaugeFactory {

    private static final String NAME = "XXX";
        @SneakyThrows
        public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
//        extract(Imgcodecs.imread("data/example/temp.jpg", Imgcodecs.IMREAD_GRAYSCALE), "li");
        AnalogOnePointer g = getGaugeWithOnePointer(Imgcodecs.imread("data/example/temp.jpg", Imgcodecs.IMREAD_GRAYSCALE));

//        HighGui.imshow("gauge",g.getSource());
//        extract(Imgcodecs.imread("data/example/testBild1.jpg"));
//        extract(Imgcodecs.imread("data/example/Li_Example_1.png"));

            System.out.println(g.getValue()+"");
//            HighGui.waitKey();
    }

    private final TextDedection TEXT_DEDECTION = new TextDedection();


    public static Gauge getGauge(Mat src) {
        val cannyEdgeDetector = getCanny();


        //1. Canny Edge Dedection mit auto Threashold
        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(src));
        cannyEdgeDetector.process();

        //2. Finden der größten Ellipse mit einem Ellipse Score über 0.8
        val biggestEllipse = getGreatestEllipse(cannyEdgeDetector);

        //3. Alles ausserhalb der Ellipse Entfernen
        val maskiert = removeAllOutsideEllpipse(src, biggestEllipse);
        val cannyMask = removeAllOutsideEllpipse(cannyEdgeDetector.getEdgeMat(), biggestEllipse);

        //4. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
        val transponiert = transponiere(maskiert, biggestEllipse);
        val cannyTransponiert = transponiere(cannyMask, biggestEllipse);

        //5. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
        val rotate = Imgproc.getRotationMatrix2D(new Point(transponiert.width() / 2.0, transponiert.height() / 2.0), 90 - biggestEllipse.angle, 1.0);
        val gedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(transponiert, gedreht, rotate, transponiert.size());
        //5.1
        val cannyGedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(cannyTransponiert, cannyGedreht, rotate, transponiert.size());

        Imgcodecs.imwrite("data/out/" +NAME+ "_1_source.png",src);
        Imgcodecs.imwrite("data/out/" +NAME+ "_2_canny.png",cannyEdgeDetector.getEdgeMat());
        Imgcodecs.imwrite("data/out/" +NAME+ "_3_ellipse.png",maskiert);
        Imgcodecs.imwrite("data/out/" +NAME+ "_4_transponiert.png",transponiert);
        Imgcodecs.imwrite("data/out/" +NAME+ "_5_gedreht.png",gedreht);

        Gauge gauge = new Gauge(
                gedreht, cannyGedreht, null);
        return gauge;
    }

    public static AnalogOnePointer getGaugeWithOnePointer(Mat src) throws NotGaugeWithPointerException {
        Gauge gauge = getGauge(src);

        //1. Erkennung der Skala
        val x = ScaleMarkExtraction.extract(gauge.getCanny(), gauge.getSource(), 4);
        val m = new MatOfPoint2f();
        m.fromList(x);


        //2. Alles ausserhalb der Ellipse Entfernen
        val e = Imgproc.fitEllipse(m);
        val maskiertSkala = removeAllOutsideEllpipse(gauge.getSource(), e);
        val cannyMaskSkala = removeAllOutsideEllpipse(gauge.getCanny(), e);

        //3. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
        val transponiertSkala = transponiere(maskiertSkala, e);
        val cannyTransponiertSkala = transponiere(cannyMaskSkala, e);

        //4. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
        val rotateSkala = Imgproc.getRotationMatrix2D(new Point(transponiertSkala.width() / 2.0, transponiertSkala.height() / 2.0), 90 - e.angle, 1.0);
        val gedrehtSkala = Mat.zeros(transponiertSkala.size(), transponiertSkala.type());
        Imgproc.warpAffine(transponiertSkala, gedrehtSkala, rotateSkala, transponiertSkala.size());
        val cannyGedrehtSkala = Mat.zeros(cannyTransponiertSkala.size(), cannyTransponiertSkala.type());
        Imgproc.warpAffine(cannyTransponiertSkala, cannyGedrehtSkala, rotateSkala, cannyTransponiertSkala.size());


        AnalogOnePointer g = new AnalogOnePointer(new Gauge(gedrehtSkala, cannyGedrehtSkala, null),new TextDedection());


        Imgcodecs.imwrite("data/out/" +NAME+ "_6_skala.png",maskiertSkala);
        Imgcodecs.imwrite("data/out/" +NAME+ "_7_skalaRotiert.png",gedrehtSkala);
        Imgcodecs.imwrite("data/out/" +NAME+ "_8_otsu.png",g.otsu);
        Imgcodecs.imwrite("data/out/" +NAME+ "_9_dedected.png",g.getDrawing(g.source.clone()));
        return g;
    }





    public static CannyEdgeDetector getCanny() {
        CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector();
//        cannyEdgeDetector.setLowThreshold(2.5f); // 2.5
//        cannyEdgeDetector.setHighThreshold(7.5f); // 7.5
        cannyEdgeDetector.setAutoThreshold(true);
        // 2 is default for CannyEdgeDetector but 1 is setting from Ellipse reference code
        cannyEdgeDetector.setGaussianKernelRadius(1f); // 2
        // 16 is default for Canny Edge Detector, but 5 is setting from ellipse reference code.
        cannyEdgeDetector.setGaussianKernelWidth(10); // 16

        return cannyEdgeDetector;
    }

    @SneakyThrows
    static public RotatedRect getGreatestEllipse(CannyEdgeDetector edgeDetector) {

        EllipseDetector ellipseDetector = new EllipseDetector();
        ellipseDetector.setEdgeImage(edgeDetector);
        ellipseDetector.setUseMedianCenter(true);
        ellipseDetector.setDistanceToEllipseContour(0.1f);
        ellipseDetector.process();

        val mat = bufferedImageToMat(edgeDetector.getEdgeImage());

        val ellipsInside = ellipseDetector.getFinalEllipseList().stream().filter(ellipse -> {
                    val e = EllipseDetector.createContour(ellipse);

                    val radius = (int) Math.max(e.size.width, e.size.height) / 2;

                    if (
                            radius > 128 &&
                                    e.center.x - radius >= 0
                                    && e.center.y - radius >= 0
                                    && e.center.x + radius < mat.width()
                                    && e.center.y + radius < mat.height()
                    ) {
                        return true;
                    } else {
                        return false;
                    }
                }

        ).sorted((o1, o2) -> (o2.ellipseScore > o1.ellipseScore) ? 1 : 0);

        return EllipseDetector.createContour(ellipsInside.findFirst().get());
    }


    public RotatedRect getGreatestEllipse(Mat input) {
        val cannyEdgeDetector = getCanny();

        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(input));
        cannyEdgeDetector.process();
        return getGreatestEllipse(cannyEdgeDetector);
    }


    public static Mat transponiere(Mat mat, RotatedRect ellipse) {
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transponiere(mat, Arrays.stream(pts).collect(Collectors.toList()));
    }

    public static Mat transponiere(Mat mat, List<Point> corner) {

        List<Point> target = new ArrayList<Point>();
        target.add(new Point(0, 0));
        target.add(new Point(mat.cols(), 0));
        target.add(new Point(mat.cols(), mat.rows()));
        target.add(new Point(0, mat.rows()));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);

        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, new Size(mat.cols(), mat.rows()));

        val maxPoints = Helper.maxDistance(corner);
        val maxDist = Helper.calculateDistanceBetweenPointsWithPoint2D(maxPoints);
        Imgproc.resize(proj, proj, new Size(maxDist, maxDist));
        return proj;
    }


    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        BufferedImage x = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        x.setData(bi.getRaster());
        byte[] data = ((DataBufferByte) x.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public static Mat removeAllOutsideEllpipse(Mat input, RotatedRect ellipse) {
        Mat out = Mat.zeros(input.size(), input.type());
        Imgproc.ellipse(out, ellipse, Helper.WHITE, -1);

        for (int i = 0; i < input.width(); i++) {
            for (int j = 0; j < input.height(); j++) {
                if (out.get(j, i)[0] == 255) {
                    out.put(j, i, input.get(j, i));
                }
            }
        }
        return out;
    }


}
