package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.popeye.transformation.Pair;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import de.burrotinto.turboSniffle.meters.gauge.test.Pointer;
import lombok.Getter;
import lombok.val;
import org.nd4j.linalg.primitives.AtomicDouble;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class Gauge {
    public final static int TYPE = CvType.CV_8U;
    public final static Size DEFAULT_SIZE = new Size(512, 512);
    public final static int AUFROLL_STEPS = 720;

    private final TextDedection textDedection;
    private final HashMap<RotatedRect, Integer> labelScale = new HashMap<>();


    @Getter
    private Mat source;
    @Getter
    private Mat canny;
//    private Mat bw;
//    private Mat ausgerolltSRC;

    //    private CannyEdgeDetector cannyEdgeDetector;
    private Pointer[] pointer = new Pointer[0];

    public Gauge(Mat source, Mat canny, TextDedection textDedection) {
        this.textDedection = textDedection;
        //Convertiere in Grau
        if (source.type() == TYPE) {
            this.source = source;
        } else {
            this.source = Mat.zeros(source.size(), TYPE);
            Imgproc.cvtColor(source, this.source, Imgproc.COLOR_BGR2GRAY);
        }
        this.canny = canny;

        Imgproc.resize(this.source, this.source, DEFAULT_SIZE);
        if (canny != null) {
            Imgproc.resize(this.canny, this.canny, DEFAULT_SIZE);
        }

        // Otsu Binary
        Imgproc.threshold(source, source, 0, 255, Imgproc.THRESH_OTSU);

        //Wenn Wenn es mehr Schwarz als weiß gibt müssen faren getauscht werden
        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);

        List<Pixel> pixels = Helper.getAllPixel(source, mask);
        if (pixels.stream().filter(pixel -> pixel.color == 0).count() > pixels.size() / 2) {
            Core.bitwise_not(source, source);
        }

        // Beschriftung erkennung
        val textAreas = DistanceToPointClusterer.extract(textDedection.getTextAreas(source), getCenter(), (int) (DEFAULT_SIZE.height / 25), 2);

        for (RotatedRect r : textAreas) {
            try {
                BufferedImage sub = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
//                HighGui.imshow(source.submat(r.boundingRect()).toString(), source.submat(r.boundingRect()));
                String str = textDedection.doOCRNumbers(sub).replaceAll("[^0-9]", "");
                Integer i = Integer.parseInt(str);

                System.out.println(i + "");
                labelScale.put(r, i);
            } catch (Exception e) {
            }

        }

    }

    public Mat toSize(Size size) {
        Mat out = new Mat();
        Imgproc.resize(source, out, size);
        return out;
    }

    public Point getCenter() {
        return new Point(source.size().width / 2, source.size().height / 2);
    }

    public double getRadius() {
        return source.size().width / 2;
    }

//    public Mat getAusgerolltSource() {
//        return ausgerolltSRC;
//    }
//
//    public Mat getAusgerolltCanny() {
//        return getAusgerollt(canny, AUFROLL_STEPS, 60);
//    }

//    public Mat getAusgerolltBW() {
//        //Canny and Contours finding
//        Mat cannyOutput = new Mat();
//        Imgproc.Canny(getAusgerolltSource(), cannyOutput, 85, 85 * 2);
//
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//        val draw = Mat.zeros(getAusgerolltSource().size(), getAusgerolltSource().type());
//        Imgproc.drawContours(draw, contours, -1, new Scalar(255, 255, 255), -1);
//
//        return draw;
//    }

//    private Mat getAusgerollt(Mat src, double steps, int cutPXBottom) {
//
//        Mat out = new Mat(new Size(steps, (Math.min(src.size().height, src.size().width) / 2) - cutPXBottom), src.type());
//        int nextcol = 0;
//        for (double i = 0; i < 360; i += 360.0 / steps) {
//            Mat rMap = new Mat();
//            val rotate = Imgproc.getRotationMatrix2D(new Point(src.size().width / 2, src.size().height / 2), i, 1.0);
//            Imgproc.warpAffine(src, rMap, rotate, src.size());
//
//            for (int j = cutPXBottom; j < (src.size().width / 2); j++) {
//                double[] x = rMap.get(j + (int) rMap.size().height / 2, (int) rMap.size().height / 2);
//                out.put((int) rMap.size().height / 2 - j, nextcol, x);
//            }
//            nextcol++;
//        }
//        return out;
//    }

    public Pointer[] getPointer() {
        if (pointer.length == 0) {
            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();

            Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

            ArrayList<Pointer> zeigerKandidaten = new ArrayList<>();

            for (MatOfPoint contour : contours) {


                Pointer p = new Pointer(contour);
                Moments moment = Imgproc.moments(contour);

                //Richtung des Zeigers ermitteln
                if (Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()) > Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) {
                    p.setArrow(p.getDirection().p1);
                    p.setBottom(p.getDirection().p2);
                } else {
                    p.setArrow(p.getDirection().p2);
                    p.setBottom(p.getDirection().p1);
                }


                MatOfPoint2f hull = new MatOfPoint2f();
                p.getContour().get(0).convertTo(hull, CvType.CV_32F);


                if (
//                        Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), p.getArrow()) < getRadius()
//                        && Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), p.getBottom()) < (getRadius() * 2) / 3
//                        && Imgproc.arcLength(hull,true) > (getRadius() * 2) / 3
                        Imgproc.pointPolygonTest(hull, getCenter(), true) >= 0
                ) {
                    zeigerKandidaten.add(p);
                }
            }

            //Längsten Kandidaten auswählen
            zeigerKandidaten.stream().max((o1, o2) -> (int) (o1.getLength() - o2.getLength())).ifPresent(pointer1 -> {
                pointer = new Pointer[1];
                pointer[0] = pointer1;
            });


        }
        return pointer;
    }

    public Mat getPointerOnlyMat() {
        Mat out = Mat.zeros(source.size(), TYPE);
        Imgproc.line(out, getCenter(), getPointer()[0].getArrow(), new Scalar(255, 255, 255), 20);
//        Imgproc.drawContours(out,getPointer()[0].getContour(),-1,,-1);

        Core.bitwise_not(out, out);
        return out;
    }

    private int longestLineFromBottom(Mat src, int col, int maxColor) {
        int lastY = src.rows() - 1;
        double lastColor = maxColor;

        while (lastY >= 0 && lastColor >= src.get(lastY, col)[0] - 100 && lastColor <= src.get(lastY, col)[0] + 50) {

            lastColor = src.get(lastY, col)[0];
            lastY--;
        }

        return src.rows() - lastY;
    }

    private double calculateWinkel(Point point) {
        double hypotenuse = Helper.calculateDistanceBetweenPointsWithPoint2D(point, getCenter());
        double ankathete = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), new Point(point.x, getCenter().y));
        double w = (180 / Math.PI) * Math.acos(ankathete / hypotenuse);
        if (point.x < getCenter().x && point.y < getCenter().y) {
            return 180 - w;
        } else if (point.x < getCenter().x && point.y > getCenter().y) {
            return 180 + w;
        } else if (point.x > getCenter().x && point.y > getCenter().y) {
            return 360 - w;
        } else {
            return w;
        }
    }

    public double getValue() {
//        double pointer = calculateWinkel(getPointer()[0].getMinRect().center);
        double pointer = getPointerAngel();

        LinkedList<Pair<Double, Map.Entry<RotatedRect, Integer>>> pairs = new LinkedList<>();
        labelScale.entrySet().stream().forEach(e -> {
            Double x = Math.abs(calculateWinkel(e.getKey().center) - pointer);
            pairs.add(new Pair<>(x, e));
            pairs.add(new Pair<>(360 - x, e));
        });

        //Sortierung nach entfernung zum Zeiger
        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));

        double minW = calculateWinkel(pairs.get(0).p2.getKey().center);
        double maxW = calculateWinkel(pairs.get(1).p2.getKey().center);


        //Bestimmen eines Wertes pro Prozent
        double xPP = Math.abs(pairs.get(0).p2.getValue() - pairs.get(1).p2.getValue()) / (Math.abs((minW - maxW) % 360));
        //Delta zum Zähler
        double x = (minW - pointer);

        return pairs.get(0).p2.getValue() + (x * xPP);
    }

    public Mat getCalcMat() {
        Mat drawing = Mat.zeros(DEFAULT_SIZE, TYPE);

        Helper.drawRotatedRectangle(drawing, getPointer()[0].getMinRect(), Helper.WHITE);

        labelScale.forEach((rotatedRect, integer) -> {
            Helper.drawRotatedRectangle(drawing, rotatedRect, Helper.WHITE);
            Imgproc.putText(drawing, "" + integer, rotatedRect.center, 0, 1, new Scalar(255, 255, 255));
        });

        return drawing;
    }


    /**
     * Chi et al.
     * Machine Vision Based Automatic Detection Method of
     * Indicating Values of a Pointer Gauge
     * <p>
     * Step 4
     */
    public Mat getScaleMarks() {
        Mat otsu = new Mat();
        Imgproc.threshold(source, otsu, 0, 255, Imgproc.THRESH_OTSU);

        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);

        List<Pixel> pixels = Helper.getAllPixel(otsu, mask).stream().filter(pixel -> pixel.color == 0).collect(Collectors.toList());

        Mat draw = Mat.zeros(DEFAULT_SIZE, TYPE);

        pixels.forEach(pixel -> Imgproc.line(draw, pixel.point, getCenter(), Helper.WHITE));

        HashMap<Double, ArrayList<Pixel>> map = new HashMap<>();
        pixels.forEach(pixel -> {
            double angle = ((int) (calculateWinkel(pixel.point) * 10)) * 0.1;
            map.putIfAbsent(angle, new ArrayList<>());
            map.get(angle).add(pixel);
        });

        return draw;
    }

    public void testObanhandVOndenErkanntenSkalenBeschriftungenDerBereichExportiertWerdenKann() {
        ArrayList<Point> points = new ArrayList<>();

        labelScale.forEach((rotatedRect, integer) -> points.add(rotatedRect.center));
        val m = new MatOfPoint2f();
        m.fromList(points);


        Mat draw = source.clone();


        val maxDist = Helper.maxDistance(points);
        int y = (int) (Math.max(maxDist.p1.y, maxDist.p2.y) - (Math.abs(maxDist.p1.y - maxDist.p2.y) / 2));
        int x = (int) (Math.max(maxDist.p1.x, maxDist.p2.x) - (Math.abs(maxDist.p1.x - maxDist.p2.x) / 2));

        Imgproc.drawMarker(draw, new Point(x, y), Helper.BLACK);

//        HighGui.imshow("eeee", draw);
    }


    /*+
    A High-Robust Automatic Reading Algorithm
of Pointer Meters Based on Text Detection
     */
    public double getPointerAngel() {
        AtomicDouble sum = new AtomicDouble(0.0);
        labelScale.forEach((rotatedRect, integer) -> sum.addAndGet(Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), rotatedRect.center)));


        Mat otsu = new Mat();
        Imgproc.threshold(source, otsu, 0, 255, Imgproc.THRESH_OTSU);


        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);
        Imgproc.circle(mask, getCenter(), (int) sum.get() / labelScale.size(), Helper.BLACK, -1);

        List<Pixel> pixels = Helper.getAllPixel(otsu, mask).stream().filter(pixel -> pixel.color == 0).collect(Collectors.toList());

        HashMap<Double, ArrayList<Pixel>> map = new HashMap<>();
        pixels.forEach(pixel -> {
            double angle = ((int) (calculateWinkel(pixel.point)));
            map.putIfAbsent(angle, new ArrayList<>());
            map.get(angle).add(pixel);
        });

        AtomicReference<Double> angle = new AtomicReference<>();

        map.forEach((aDouble, pixels1) -> {
            if (angle.get() == null || pixels1.size() > map.get(angle.get()).size()) {
                angle.set(aDouble);
            }
        });

        System.out.println("Winkel " + angle.get());
        return angle.get();

    }

    public Point poolarZuKartesisch(double winkel, double r) {
        double x = r * Math.cos(Math.toRadians(winkel));
        double y = r * Math.sin(Math.toRadians(winkel));
        System.out.println("x=" + x + "; y=" + y);
        return new Point(x + getCenter().x, getCenter().y - y);

    }

    public Mat getFinalDedectedGauge() {
        Mat drawing = Mat.zeros(DEFAULT_SIZE, TYPE);
        AtomicInteger dist = new AtomicInteger(0);
        AtomicInteger i = new AtomicInteger(0);
        labelScale.forEach((rotatedRect, integer) -> {
            Imgproc.drawMarker(drawing, rotatedRect.center, Helper.WHITE, Imgproc.MARKER_CROSS);
            Imgproc.putText(drawing, String.valueOf(integer), rotatedRect.center, 0, 1.0, Helper.WHITE);
            dist.addAndGet((int) Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()));
            i.incrementAndGet();
        });

        int r = dist.get()/i.get();
        Imgproc.arrowedLine(drawing, getCenter(), poolarZuKartesisch(getPointerAngel(), r), Helper.WHITE, 3);
        Imgproc.putText(drawing, String.valueOf(Math.round(getValue()*100)/100.00),  poolarZuKartesisch(getPointerAngel(), r/2), 0, 1.0, Helper.WHITE);

        return drawing;
    }
}
