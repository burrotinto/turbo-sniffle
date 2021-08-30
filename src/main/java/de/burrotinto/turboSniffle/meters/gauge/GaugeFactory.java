package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.ellipse.CannyEdgeDetector;
import de.burrotinto.turboSniffle.ellipse.EllipseDetector;
import de.burrotinto.turboSniffle.meters.gauge.impl.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.meters.gauge.impl.HeatMap;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.GaugeTwoPointerLearningDataset;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.TrainingSet;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.stream.Collectors;


public class GaugeFactory {
    private static final int BILATERAL_D = 20; //20

    public static final TextDedection TEXT_DEDECTION = new TextDedection();


    public static Gauge getGauge(Mat src) {
        Gauge out = null;
        try {
            out = getGaugeEllipseMethod(src, BILATERAL_D);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (out == null) {
                out = getGaugeWithHeatMap(src, 20);
            } else {
                out = getGaugeWithHeatMap(out.source, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static Gauge getGaugeEllipseMethod(Mat src, int bilateralD) {
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

    public static ValueGauge getGaugeWithOnePointerNoScale(Gauge gauge) throws NotGaugeWithPointerException {
        return new GaugeOnePointerNoScale(gauge);
    }


    public static ValueGauge getGaugeWithOnePointerAutoScale(Gauge gauge, Optional<Double> steps, Optional<Double> min, Optional<Double> max) throws NotGaugeWithPointerException {
        return new GaugeOnePointerAutoScale(gauge, TEXT_DEDECTION, steps, min, max);
    }

    public static ValueGauge getGaugeWithOnePointerAutoScale(Mat src) throws NotGaugeWithPointerException {
        return getGaugeWithOnePointerAutoScale(getGauge(src), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static ValueGauge getGaugeWithOnePointerAutoScale(Gauge gauge) throws NotGaugeWithPointerException {
        return getGaugeWithOnePointerAutoScale(gauge, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static Cessna172AirspeedIndecator getCessna172AirspeedIndecator(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172AirspeedIndecator(getGaugeWithHeatMap(src, -1), TEXT_DEDECTION);
    }

    public static Cessna172VerticalSpeedIndicator getCessna172VerticalSpeedIndicator(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172VerticalSpeedIndicator(getGaugeWithHeatMap(src, -1), TEXT_DEDECTION);
    }

    public static AutoEncoderGauge getCessna172Altimeter(Mat src) {
        return getAutoencoderGauge(getGaugeWithHeatMap(src, -1), GaugeTwoPointerLearningDataset.get(),12);
    }

    @SneakyThrows
    public static AutoEncoderGauge getAutoencoderGauge(Gauge gauge, TrainingSet trainingSet,int hiddenLayer) {
        return new AutoEncoderGauge(gauge, trainingSet,hiddenLayer);
    }

    @SneakyThrows
    public static AutoEncoderGauge getAutoencoderGauge(Mat mat, TrainingSet trainingSet,int hiddenLayer) {
        return new AutoEncoderGauge(getGauge(mat), trainingSet,hiddenLayer);
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
                    Point[] p = new Point[4];
                    e.points(p);
                    val radius = (int) Math.max(e.size.width, e.size.height) / 2;

                    // Größe noch sinnvoll und innerhalb der MAT
                    if (
                            e.size.area() > Gauge.DEFAULT_SIZE.area() / 2 &&
                                    EllipseDetector.getAllEllipsePoints(ellipse).stream().allMatch(point ->
                                            point.x >= 0
                                                    && point.x < mat.width()
                                                    && point.y >= 0
                                                    && point.y < mat.height())
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
        target.add(new Point(Gauge.DEFAULT_SIZE.width, 0));
        target.add(new Point(Gauge.DEFAULT_SIZE.width, Gauge.DEFAULT_SIZE.height));
        target.add(new Point(0, Gauge.DEFAULT_SIZE.height));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);

        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, Gauge.DEFAULT_SIZE);

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

            Imgproc.createCLAHE(2.0, new Size(8, 8)).apply(bilateral, bilateral);
        } else {
            bilateral = src.clone();
        }


        Mat canny = new Mat(src.size(), Gauge.TYPE);
        Imgproc.Canny(bilateral, canny, 255 / 3, 120);

        HeatMap heatMap = new HeatMap(canny);

        val points = new ArrayList<Point>();


        double dist = 0;
        double min = Double.MAX_VALUE;
        val cluster = DistanceToPointClusterer.extractWithArea(heatMap.getAllConnectedWithCenter(), heatMap.getCenter(), 40, 2);


        //Median
        cluster.sort(Comparator.comparingDouble(o -> Helper.calculateDistanceBetweenPointsWithPoint2D(o.center, heatMap.getCenter())));
        dist = Helper.calculateDistanceBetweenPointsWithPoint2D(cluster.get(cluster.size() / 2).center, heatMap.getCenter());


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

        gauge.setHeatMap(heatMap);

        return gauge;
    }
}
