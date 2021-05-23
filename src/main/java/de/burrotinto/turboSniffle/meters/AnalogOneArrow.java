//package de.burrotinto.turboSniffle.meters;
//
//import de.burrotinto.turboSniffle.cv.TextDedection;
//import de.burrotinto.turboSniffle.cv.Helper;
//import de.burrotinto.popeye.transformation.Pair;
//import lombok.SneakyThrows;
//import lombok.val;
//import net.sourceforge.tess4j.Tesseract;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfKeyPoint;
//import org.opencv.core.MatOfPoint;
//import org.opencv.core.MatOfPoint2f;
//import org.opencv.core.Point;
//import org.opencv.core.Rect;
//import org.opencv.core.RotatedRect;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.features2d.Features2d;
//import org.opencv.features2d.SimpleBlobDetector;
//import org.opencv.highgui.HighGui;
//import org.opencv.imgproc.Imgproc;
//
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Random;
//
//
//public class AnalogOneArrow extends CircleGauge implements Measuring {
//    private final Tesseract tesseract = new Tesseract();
//    private final TextDedection textDedection = new TextDedection();
//    private final HashMap<RotatedRect, Integer> labels = new HashMap<>();
//    private final int steps;
//
//    private Mat cannyOutput;
//    private int threshold;
//    private RotatedRect minEllipseDisplay;
//    private List<MatOfPoint> contours = new ArrayList<>();
//    private int indexDisplay = 0;
//
//    private Optional<Pointer> pointer = Optional.empty();
//
//
//    public AnalogOneArrow(Mat src) {
//        this(src, 100, 10);
//    }
//
//    @SneakyThrows
//    public AnalogOneArrow(Mat inputSrc, int threshold, int steps) {
//        super(inputSrc,threshold);
//        this.threshold = threshold;
//        this.steps = steps;
//
//        update(inputSrc);
////        val maxQuadrat = new Rect(0, 0, Math.min(inputSrc.width(), inputSrc.height()), Math.min(inputSrc.width(), inputSrc.height()));
////        this.src = inputSrc.submat(maxQuadrat);
////        Imgproc.resize(this.src, this.src, new Size(512, 512));
////        this.threshold = threshold;
////
////        cannyOutput = new Mat();
////
////        Imgproc.Canny(this.src, cannyOutput, threshold, threshold * 2);
////        List<MatOfPoint> contours = new ArrayList<>();
////        Mat hierarchy = new Mat();
////        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
////
////
////        //Check und initialisierung der Anzeige
////        RotatedRect[] minEllipse = new RotatedRect[contours.size()];
////        int indexMax = 0;
////        for (int i = 0; i < contours.size(); i++) {
////            minEllipse[i] = new RotatedRect();
////            if (contours.get(i).rows() > 5) {
////                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
////            }
////            if (minEllipse[i].size.area() > minEllipse[indexMax].size.area()) {
////                indexMax = i;
////            }
////        }
////        minEllipseDisplay = minEllipse[indexMax];
////
////
////        //Zeiger Check und initialisierung
////        ArrayList<Pointer> zeigerKandidaten = new ArrayList<>();
////        for (MatOfPoint contour : contours) {
////            int finalIndexMax = indexMax;
////            Pointer.isPointer(contour).ifPresent(p -> {
////
////                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(finalIndexMax).toArray()), p.getDirection().p1, false) >= 0
////                        && Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(finalIndexMax).toArray()), p.getDirection().p2, false) >= 0
////                        && Math.min(Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()), Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) <= getDisplay().size.width / 3
////                ) {
////                    zeigerKandidaten.add(p);
////                }
////            });
////        }
////        zeigerKandidaten.stream().max((o1, o2) -> (int) (o1.getLength() - o2.getLength())).ifPresent(p -> pointer = p);
////
////        if (Helper.calculateDistanceBetweenPointsWithPoint2D(pointer.getDirection().p1, getCenter()) > Helper.calculateDistanceBetweenPointsWithPoint2D(pointer.getDirection().p2, getCenter())) {
////            pointer.setArrow(pointer.getDirection().p1);
////            pointer.setBottom(pointer.getDirection().p2);
////        } else {
////            pointer.setArrow(pointer.getDirection().p2);
////            pointer.setBottom(pointer.getDirection().p1);
////        }
//
//
////        Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(indexMax).toArray()), new Point(0, 0), false);
//
//        tesseract.setDatapath("data");
//        tesseract.setLanguage("digits_comma");
//
//
////        for (RotatedRect r : textDedection.getTextAreas(src)) {
////            try {
////                BufferedImage sub = Helper.Mat2BufferedImage(src.submat(r.boundingRect()));
////
////                String str = it.doOCR(sub).replaceAll("[^0-9]", "");
////                Integer i = Integer.parseInt(str);
////                if (i % steps == 0) {
////                    labels.put(r, i);
////                }
////            } catch (Exception e) {
////            }
////
////        }
//
//    }
//
//    public RotatedRect getDisplay() {
//        return minEllipseDisplay;
//    }
//
//    public Point getCenter() {
//        return minEllipseDisplay.center;
//    }
//
//    public Pointer getPointer() {
//        if (pointer.isEmpty()) {
//
//            ArrayList<Pointer> zeigerKandidaten = new ArrayList<>();
//            for (MatOfPoint contour : contours) {
//                int finalIndexMax = indexDisplay;
//                Pointer.isPointer(contour).ifPresent(p -> {
//
//                    if (Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(finalIndexMax).toArray()), p.getDirection().p1, false) >= 0
//                            && Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(finalIndexMax).toArray()), p.getDirection().p2, false) >= 0
//                            && Math.min(Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()), Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) <= getDisplay().size.width / 3
//                    ) {
//                        zeigerKandidaten.add(p);
//                    }
//                });
//            }
//            //Längsten Kandidaten auswählen
//            pointer = zeigerKandidaten.stream().max((o1, o2) -> (int) (o1.getLength() - o2.getLength()));
//
//            //Richtung des Zeigers ermitteln
//            pointer.ifPresent(p -> {
//                if (Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()) > Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) {
//                    p.setArrow(p.getDirection().p1);
//                    p.setBottom(p.getDirection().p2);
//                } else {
//                    p.setArrow(p.getDirection().p2);
//                    p.setBottom(p.getDirection().p1);
//                }
//            });
//
//        }
//
//        return pointer.orElse(null);
//    }
//
//    public double getWinkel() {
//        return calculateWinkel(pointer.get().getArrow());
//    }
//
//    public Mat getDrawing() {
//        Mat drawing = Mat.zeros(src.size(), CvType.CV_8UC3);
//        val rng = new Random();
//
//        Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
//
//        Imgproc.ellipse(drawing, getDisplay(), color, 2);
//        // rotated rectangle
//        Imgproc.drawMarker(drawing, getCenter(), color);
//        Imgproc.line(drawing, getPointer().getArrow(), getCenter(), color, 10);
//        Imgproc.circle(drawing, getCenter(), (int) Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), getPointer().getArrow()), color);
//
//        Imgproc.drawMarker(drawing, getPointer().getArrow(), new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));
////        Imgproc.putText(drawing, getWinkel() + "°", getCenter(), 0, 1,new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));
//
//        Imgproc.putText(drawing, (int) getValue() + " ", getCenter(), 0, 1, new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));
//        getLabels().entrySet().stream().forEach(entry -> {
//            Imgproc.putText(drawing, entry.getValue().toString(), entry.getKey().center, 0, 0.5, new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));
//        });
//        return drawing;
//    }
//
//    private double calculateWinkel(Point point) {
//        double hypotenuse = Helper.calculateDistanceBetweenPointsWithPoint2D(point, getCenter());
//        double ankathete = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), new Point(point.x, getCenter().y));
//        double w = (180 / Math.PI) * Math.acos(ankathete / hypotenuse);
//        if (point.x < getCenter().x && point.y < getCenter().y) {
//            return 180 - w;
//        } else if (point.x < getCenter().x && point.y > getCenter().y) {
//            return 180 + w;
//        } else if (point.x > getCenter().x && point.y > getCenter().y) {
//            return 360 - w;
//        } else {
//            return w;
//        }
//    }
//
//    public double getValue() {
//        double pointer = getWinkel();
//
//        LinkedList<Pair<Double, Map.Entry<RotatedRect, Integer>>> pairs = new LinkedList<>();
//        getLabels().entrySet().stream().forEach(e -> {
//            Double x = Math.abs(calculateWinkel(e.getKey().center) - pointer);
//            pairs.add(new Pair<>(x, e));
//            pairs.add(new Pair<>(360 - x, e));
//        });
//        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));
//
//
//        double minW = calculateWinkel(pairs.get(0).p2.getKey().center);
//        double maxW = calculateWinkel(pairs.get(1).p2.getKey().center);
//
//
//        double xPP = Math.abs(pairs.get(0).p2.getValue() - pairs.get(1).p2.getValue()) / Math.abs((minW - maxW + 360) % 360);
//        double x = (minW - pointer);
//
//        return pairs.get(0).p2.getValue() + (x * xPP);
//    }
//
//    @Override
//    public void update(Mat mat) {
//        //initialisation
//        val maxQuadrat = new Rect(0, 0, Math.min(mat.width(), mat.height()), Math.min(mat.width(), mat.height()));
//        super.update(mat.submat(maxQuadrat));
//
//
//        Imgproc.resize(src, src, new Size(512, 512));
//
//        pointer = Optional.empty();
//        indexDisplay = 0;
//
//
//        //Canny and Contours finding
//        cannyOutput = new Mat();
//        Imgproc.Canny(this.src, cannyOutput, threshold, threshold * 2);
//        contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//
//        //Check und initialisierung der Anzeige
//        RotatedRect[] minEllipse = new RotatedRect[contours.size()];
//
//        for (int i = 0; i < contours.size(); i++) {
//            minEllipse[i] = new RotatedRect();
//            if (contours.get(i).rows() > 5) {
//                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
//            }
//            if (minEllipse[i].size.area() > minEllipse[indexDisplay].size.area()) {
//                indexDisplay = i;
//            }
//        }
//        minEllipseDisplay = minEllipse[indexDisplay];
//
//
//        for (RotatedRect r : textDedection.getTextAreas(src)) {
//            try {
//                BufferedImage sub = Helper.Mat2BufferedImage(src.submat(r.boundingRect()));
//
//                String str = tesseract.doOCR(sub).replaceAll("[^0-9]", "");
//                Integer i = Integer.parseInt(str);
//                if (i % steps == 0) {
//                    labels.put(r, i);
//                }
//            } catch (Exception e) {
//            }
//
//        }
//
//    }
//
//    public Map<RotatedRect, Integer> getLabels() {
//        return labels;
//    }
//}
