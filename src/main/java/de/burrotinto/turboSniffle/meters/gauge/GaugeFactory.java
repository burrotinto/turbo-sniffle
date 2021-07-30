package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.ellipse.CannyEdgeDetector;
import de.burrotinto.turboSniffle.ellipse.EllipseDetector;
import de.burrotinto.turboSniffle.meters.gauge.impl.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.meters.gauge.impl.ScaleMarkExtraction;
import de.burrotinto.turboSniffle.meters.gauge.test.HeatMap;
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
import java.util.Optional;
import java.util.stream.Collectors;


public class GaugeFactory {
    private static final int BILATERAL_D = 20; //20
    private static final String FILE = "data/example/gauge/value=38_min=0_max=100_step=20_id=0.jpg";
    //    private static final String FILE = "data/example/0_bar_I.jpg";
    private static final String NAME = FILE.split("/")[FILE.split("/").length - 1].split("\\.")[0];

    public static final TextDedection TEXT_DEDECTION = new TextDedection();

    @SneakyThrows
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

//        extract(Imgcodecs.imread("data/example/21_C.jpg", Imgcodecs.IMREAD_GRAYSCALE), "li");
        Mat x = Imgcodecs.imread(FILE, Imgcodecs.IMREAD_GRAYSCALE);
        Imgcodecs.imwrite("data/out/CLAHE_org.png", x);

        Imgproc.createCLAHE().apply(x, x);
        Imgcodecs.imwrite("data/out/CLAHE_filter.png", x);
    }

    public static Gauge getGauge(Mat src) {
        return getGauge(src, BILATERAL_D);
    }

    public static Gauge getGauge(Mat src, int bilateralD) {
        CannyEdgeDetector cannyEdgeDetector = null;
        Mat bilateral = new Mat();


        //Rauschen mittels einen bilateralen Filter entfernen
        if (bilateralD > 0) {
            Imgproc.bilateralFilter(src, bilateral, bilateralD, bilateralD * 2.0, bilateralD * 0.5);
        } else {
            bilateral = src.clone();
        }
        double i = 0;
        RotatedRect biggestEllipse = null;


        //https://docs.opencv.org/3.1.0/d5/daf/tutorial_py_histogram_equalization.html
        Imgproc.createCLAHE(2.0, new Size(8, 8)).apply(bilateral, bilateral);


        while (biggestEllipse == null && i < 100) {
            Mat e = new Mat();
            Core.convertScaleAbs(bilateral, e, i);
            i += 0.5;
            //1. Canny Edge Dedection mit auto Threashold
            cannyEdgeDetector = getCanny();
            cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(e));
            cannyEdgeDetector.process();

            try {
                //2. Finden der größten Ellipse mit einem Ellipse Score über 0.8
                biggestEllipse = getGreatestEllipse(cannyEdgeDetector);
            } catch (Exception ex) {
//                System.out.println(i);
            }

        }
//        Helper.drawRotatedRectangle(bilateral,biggestEllipse,new Scalar(125,125,125),10);
//        Imgproc.ellipse(bilateral,biggestEllipse,Helper.WHITE,10);
//        Imgcodecs.imwrite("data/out/ELLIPSE.png", bilateral);

        //3. Alles ausserhalb der Ellipse Entfernen
        val maskiert = removeAllOutsideEllpipse(bilateral, biggestEllipse);
        val cannyMask = removeAllOutsideEllpipse(cannyEdgeDetector.getEdgeMat(), biggestEllipse);

        //4. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
        val transponiert = transformieren(maskiert, biggestEllipse);
        val cannyTransponiert = transformieren(cannyMask, biggestEllipse);

        //5. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
        val rotate = Imgproc.getRotationMatrix2D(new Point(transponiert.width() / 2.0, transponiert.height() / 2.0), 90 - biggestEllipse.angle, 1.0);
        val gedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(transponiert, gedreht, rotate, transponiert.size());
        //5.1
        val cannyGedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(cannyTransponiert, cannyGedreht, rotate, transponiert.size());

        Gauge gauge = new Gauge(
                gedreht, cannyGedreht, null);

        return gauge;
    }

    public static Gauge getGaugeScaleFocused(Gauge gauge) {

        //1. Erkennung der Skala
        try {
            val x = ScaleMarkExtraction.extract(gauge.getCanny(), gauge.getSource(), 4);

            ArrayList<Point> points = new ArrayList<>();
            x.stream().forEach(rechteckCluster -> {
                Point[] p = new Point[4];
                rechteckCluster.points(p);
                points.addAll(Arrays.asList(rechteckCluster.center));
            });

            val m = new MatOfPoint2f();
            m.fromList(points);


            //2. Alles ausserhalb der Ellipse Entfernen
            Mat sr = gauge.getSource().clone();
            val e = Imgproc.fitEllipse(m);


            val maskiertSkala = removeAllOutsideEllpipse(sr, e);
            val cannyMaskSkala = removeAllOutsideEllpipse(gauge.getCanny(), e);

            //3. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
            val transponiertSkala = transformieren(maskiertSkala, e);
            val cannyTransponiertSkala = transformieren(cannyMaskSkala, e);

            //4. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
            val rotateSkala = Imgproc.getRotationMatrix2D(new Point(transponiertSkala.width() / 2.0, transponiertSkala.height() / 2.0), 90 - e.angle, 1.0);

            val gedrehtSkala = Mat.zeros(transponiertSkala.size(), transponiertSkala.type());
            Imgproc.warpAffine(transponiertSkala, gedrehtSkala, rotateSkala, transponiertSkala.size());
            val cannyGedrehtSkala = Mat.zeros(cannyTransponiertSkala.size(), cannyTransponiertSkala.type());
            Imgproc.warpAffine(cannyTransponiertSkala, cannyGedrehtSkala, rotateSkala, cannyTransponiertSkala.size());


            Gauge g = new Gauge(gedrehtSkala, cannyGedrehtSkala, null);

            return g;
        } catch (Exception e) {
            return gauge;
        }
    }

    public static GaugeOnePointer getGaugeWithOnePointerAutoScale(Gauge gauge, Optional<Double> steps, Optional<Double> min, Optional<Double> max) throws NotGaugeWithPointerException {
        return new GaugeOnePointerAutoScale(gauge, TEXT_DEDECTION, steps, min, max);
    }

    public static GaugeOnePointer getGaugeWithOnePointerAutoScale(Mat src) throws NotGaugeWithPointerException {
        return getGaugeWithOnePointerAutoScale(getGauge(src), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static Cessna172AirspeedIndecator getCessna172AirspeedIndecator(Mat src) throws NotGaugeWithPointerException {

        return new Cessna172AirspeedIndecator(getGaugeWithHeatMap(src, -1), TEXT_DEDECTION);
//        return new Cessna172AirspeedIndecator(getGauge(src, 2), TEXT_DEDECTION);
    }

    public static Cessna172VerticalSpeedIndicator getCessna172VerticalSpeedIndicator(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172VerticalSpeedIndicator(getGaugeWithHeatMap(src, -1), TEXT_DEDECTION);
    }

    public static CannyEdgeDetector getCanny() {
        CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector();
//        cannyEdgeDetector.setLowThreshold(2.5f); // 2.5
//        cannyEdgeDetector.setHighThreshold(7.5f); // 7.5
        cannyEdgeDetector.setAutoThreshold(true);
        // 2 is default for CannyEdgeDetector but 1 is setting from Ellipse reference code
        cannyEdgeDetector.setGaussianKernelRadius(1f); // 2
        // 16 is default for Canny Edge Detector, but 5 is setting from ellipse reference code.
        cannyEdgeDetector.setGaussianKernelWidth(5); // 16

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
                            e.size.area() > Gauge.DEFAULT_SIZE.area() / 2 &&
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

        ).sorted((o1, o2) -> (Double.compare(o2.ellipseScore, o1.ellipseScore)));
//        ).sorted((o1, o2) -> Double.compare(EllipseDetector.createContour(o2).size.area() ,EllipseDetector.createContour(o1).size.area()));

        return EllipseDetector.createContour(ellipsInside.findFirst().get());
    }


    public RotatedRect getGreatestEllipse(Mat input) {
        val cannyEdgeDetector = getCanny();

        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(input));
        cannyEdgeDetector.process();
        return getGreatestEllipse(cannyEdgeDetector);
    }


    public static Mat transformieren(Mat mat, RotatedRect ellipse) {
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transformieren(mat, Arrays.stream(pts).collect(Collectors.toList()));
    }

    public static Mat transformieren(Mat mat, List<Point> corner) {

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

        Core.bitwise_and(input, out, out);

        return out;
    }


    public static Gauge getGaugeWithHeatMap(Mat src, int bilateralD) {

        Mat bilateral = new Mat();
        //Rauschen mittels einen bilateralen Filter entfernen
        if (bilateralD > 0) {
            Imgproc.bilateralFilter(src, bilateral, bilateralD, bilateralD * 2.0, bilateralD * 0.5);
        } else {
            bilateral = src.clone();
        }

        Imgproc.createCLAHE(2.0, new Size(8, 8)).apply(bilateral, bilateral);

        Mat canny = new Mat(src.size(), Gauge.TYPE);
        Imgproc.Canny(bilateral, canny, 85, 120);
        HeatMap heatMap = new HeatMap(canny);
        val points = new ArrayList<Point>();


        double dist = 0;
        double min = Double.MAX_VALUE;
        val cluster = DistanceToPointClusterer.extract(heatMap.getAllConnectedWithCenter(), heatMap.getCenter(), 10, 5);
        for (int j = 0; j < cluster.size(); j++) {
            points.add(cluster.get(j).center);
            double toCenter = Helper.calculateDistanceBetweenPointsWithPoint2D(cluster.get(j).center, heatMap.getCenter());
            dist += toCenter * (1.0 / cluster.size());
            min = Math.min(toCenter, min);
        }


        points.add(new Point(heatMap.getCenter().x-min,heatMap.getCenter().y -min));
        points.add(new Point(heatMap.getCenter().x+min,heatMap.getCenter().y+min));
        points.add(new Point(heatMap.getCenter().x-min,heatMap.getCenter().y+min));
        points.add(new Point(heatMap.getCenter().x+min,heatMap.getCenter().y-min));

        val m = new MatOfPoint2f();
        m.fromList(points);


        RotatedRect biggestEllipse = new RotatedRect(heatMap.getCenter(), new Size((int) dist * 2, (int) dist * 2), 0);
//        RotatedRect biggestEllipse = Imgproc.fitEllipse(m);

        //3. Alles ausserhalb der Ellipse Entfernen
        val maskiert = removeAllOutsideEllpipse(bilateral, biggestEllipse);
        val cannyMask = removeAllOutsideEllpipse(canny, biggestEllipse);

        //4. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
        val transponiert = transformieren(maskiert, biggestEllipse);
        val cannyTransponiert = transformieren(cannyMask, biggestEllipse);

        //5. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
        val rotate = Imgproc.getRotationMatrix2D(new Point(transponiert.width() / 2.0, transponiert.height() / 2.0), 90 - biggestEllipse.angle, 1.0);
        val gedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(transponiert, gedreht, rotate, transponiert.size());
        //5.1
        val cannyGedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(cannyTransponiert, cannyGedreht, rotate, transponiert.size());

        Gauge gauge = new Gauge(
                gedreht, cannyGedreht, null);

        return gauge;
    }


//    public static Gauge getGaugeWithHeatMap(Mat src, int bilateralD) {
//
//        Mat bilateral = new Mat();
//        //Rauschen mittels einen bilateralen Filter entfernen
//        if (bilateralD > 0) {
//            Imgproc.bilateralFilter(src, bilateral, bilateralD, bilateralD * 2.0, bilateralD * 0.5);
//        } else {
//            bilateral = src.clone();
//        }
//
//        Imgproc.createCLAHE(2.0, new Size(8, 8)).apply(bilateral, bilateral);
//
//        Mat canny = new Mat(src.size(), Gauge.TYPE);
//        Imgproc.Canny(bilateral, canny, 85, 120);
//        HeatMap heatMap = new HeatMap(canny);
//        val points = new ArrayList<Point>();
//
//
//        double dist = 0;
//        val cluster = DistanceToPointClusterer.extract(heatMap.getAllConnectedWithCenter(), heatMap.getCenter(), 10, 5);
//        for (int j = 0; j < cluster.size(); j++) {
//            points.add(cluster.get(j).center);
//            dist += Helper.calculateDistanceBetweenPointsWithPoint2D(cluster.get(j).center, heatMap.getCenter()) * (1.0 / cluster.size());
//        }
//
//
//        val m = new MatOfPoint2f();
//        m.fromList(points);
//
//        RotatedRect biggestEllipse = new RotatedRect(heatMap.getCenter(), new Size((int) dist * 2, (int) dist * 2), 0);
//
//        //3. Alles ausserhalb der Ellipse Entfernen
//        val maskiert = removeAllOutsideEllpipse(bilateral, biggestEllipse);
//        val cannyMask = removeAllOutsideEllpipse(canny, biggestEllipse);
//
//        //4. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
//        val transponiert = transformieren(maskiert, biggestEllipse);
//        val cannyTransponiert = transformieren(cannyMask, biggestEllipse);
//
//        //5. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
//        val rotate = Imgproc.getRotationMatrix2D(new Point(transponiert.width() / 2.0, transponiert.height() / 2.0), 90 - biggestEllipse.angle, 1.0);
//        val gedreht = Mat.zeros(transponiert.size(), transponiert.type());
//        Imgproc.warpAffine(transponiert, gedreht, rotate, transponiert.size());
//        //5.1
//        val cannyGedreht = Mat.zeros(transponiert.size(), transponiert.type());
//        Imgproc.warpAffine(cannyTransponiert, cannyGedreht, rotate, transponiert.size());
//
//        Gauge gauge = new Gauge(
//                gedreht, cannyGedreht, null);
//
//        return gauge;
//    }
}
