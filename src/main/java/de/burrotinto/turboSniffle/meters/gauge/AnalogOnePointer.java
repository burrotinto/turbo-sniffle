package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import de.burrotinto.turboSniffle.meters.gauge.test.Pointer;
import lombok.val;
import org.apache.commons.math3.util.Precision;
import org.nd4j.linalg.primitives.AtomicDouble;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class AnalogOnePointer extends Gauge {
    private final double GENAUIGKEIT = 2;
    private final Size AUTOENCODER_INPUT_SIZE = new Size(128, 128);


    private final TextDedection textDedection;
    private final HashMap<RotatedRect, Double> labelScale = new HashMap<>();

    private Optional<Double> pointerAngel = Optional.empty();
    private Optional<Mat> idealisierteDarstellung = Optional.empty();
    private Optional<Double> skalemarkSteps = Optional.empty(), min = Optional.empty(), max = Optional.empty();


    AnalogOnePointer(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        this(gauge, textDedection, Optional.of(5.0), Optional.of(0.0), Optional.empty());
    }

    AnalogOnePointer(Gauge gauge, TextDedection textDedection, Optional<Double> steps, Optional<Double> min, Optional<Double> max) throws NotGaugeWithPointerException {
        super(gauge.source, gauge.canny, gauge.otsu);
        this.textDedection = textDedection;
        this.skalemarkSteps = steps;
        this.min = min;
        this.max = max;

        // Beschriftung erkennung
//        val textAreas = DistanceToPointClusterer.extract(textDedection.getTextAreas(otsu), getCenter(), (int) (DEFAULT_SIZE.height / 20), 2);
        val textAreas = textDedection.getTextAreas(otsu);
        textAreas.addAll(textDedection.getTextAreas(source));
        Collections.shuffle(textAreas);

        Mat ideal = otsu.clone();

        for (RotatedRect r : textAreas) {
            try {
                BufferedImage sub = Helper.Mat2BufferedImage(otsu.submat(r.boundingRect()));
//                Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                String str = textDedection.doOCRNumbers(sub);
                Double i = Double.parseDouble(str);
                if (addToScaleMark(r, i)) {
                    System.out.println("OTSU_OCR =" + i);
                    Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                }
            } catch (Exception e) {
                try {
                    BufferedImage sub2 = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
                    String str2 = textDedection.doOCRNumbers(sub2);
                    Double i2 = Double.parseDouble(str2);
                    if (addToScaleMark(r, i2)) {
                        System.out.println("GREYSCAKLE_OCR=" + i2);
                        Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                    }
                } catch (Exception e2) {
                }
            }
//            Skalenbeschriftung entfernen

        }

        if (labelScale.size() < 1) {
            throw new NotGaugeWithPointerException();
        }

        //Alles innerhalb der Skalenbeschriftung maskieren
        AtomicDouble sum = new AtomicDouble(0);
        labelScale.forEach((rotatedRect, integer) -> {

//            double dist = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), rotatedRect.center);
//            sum.addAndGet(dist);

        });
        val mindist = labelScale.keySet().stream().map(rotatedRect -> {
            Point[] p = new Point[4];
            rotatedRect.points(p);
            return Helper.minDistance(Arrays.asList(p), getCenter());
        }).min(Double::compareTo);
        Imgproc.circle(ideal, getCenter(), mindist.orElse(getRadius() / 2).intValue(), Helper.WHITE, -1);
//        Imgproc.circle(ideal, getCenter(), (int) getRadius() / 2, Helper.WHITE, -1);
        idealisierteDarstellung = Optional.ofNullable(ideal);
    }

    private boolean addToScaleMark(RotatedRect rect, Double scale) {

        if (scale.toString().length() < 6 //Maximale Anzahl an Zahlen plus komma
                && (skalemarkSteps.isEmpty() || scale % skalemarkSteps.get() == 0) //Lineare Aufteilung des Messbereichs
                && !labelScale.values().contains(scale) //Nur einmal darf eine Zahl vorkommen
                && (min.isEmpty() || min.get() <= scale)
                && (max.isEmpty() || max.get() >= scale)
                && (labelScale.isEmpty() || Helper.minDistance(labelScale.keySet().stream().map(rotatedRect -> rotatedRect.center).collect(Collectors.toList()), rect.center) > Math.max(rect.size.width, rect.size.height)) //Abstand zur nächsten Zahl
        ) {
            labelScale.put(rect, scale);
            return true;
        }
        return false;
    }


    /**
     * Unsupervised learning
     *
     * @return
     */
    public double getPointerAngel() {
        if (pointerAngel.isEmpty()) {

            //Todo Parallelisieren
            AtomicReference<Pair<Double, Integer>> max = new AtomicReference<>(new Pair<>(0.0, 0));

            Mat autoencoderInput = getIdealisierteDarstellung().clone();

            Imgproc.resize(autoencoderInput, autoencoderInput, AUTOENCODER_INPUT_SIZE);

//            HighGui.imshow("autoinput"+autoencoderInput.hashCode(), autoencoderInput);
//            HighGui.waitKey();

            GaugeOnePointerLearningDataset.get().getTrainingset(AUTOENCODER_INPUT_SIZE, GENAUIGKEIT).forEach((aDouble, mat) -> {
                int p = Helper.countPixel(autoencoderInput, mat, Helper.BLACK);
                if (max.get().p2 < p) {
                    max.set(new Pair<>(aDouble, p));
                }
            });

            pointerAngel = Optional.ofNullable(max.get().p1);

//            pointerAngel = Optional.ofNullable(calculateWinkel(getPointer().getMinRect().center));
        }
        return pointerAngel.get();

    }


    private List<Pixel> getPixelsForAngle(List<Pixel> pixels, double angle, int nk) {
        return pixels.stream().filter(pixel -> Math.abs(Precision.round(calculateWinkel(pixel.point), nk) % 360 - Precision.round(angle, nk) % 360) < Math.pow(10, -nk)).collect(Collectors.toList());
    }

    public double getValue() {
        double pointer = getPointerAngel();


        LinkedList<Pair<Double, Map.Entry<RotatedRect, Double>>> pairs = new LinkedList<>();
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
        double deltaMin = minW - pointer;
        double deltaMax = maxW - pointer;

        //Min / Max respektieren
        Double value = pairs.get(0).p2.getValue() + (deltaMin * xPP);
        Double valueMax = pairs.get(1).p2.getValue() + (deltaMax * xPP);

        Double richtungskorrigiert = value;
        if (max.isPresent() && value > max.get() && min.isPresent()) {
            richtungskorrigiert = Math.abs(value - max.get()) <= Math.abs(valueMax - min.get()) ? value : valueMax;
        } else if (min.isPresent() && value < min.get() && max.isPresent()) {
            richtungskorrigiert = Math.abs(value - min.get()) <= Math.abs(valueMax - max.get()) ? value : valueMax;
        }

        AtomicDouble rK = new AtomicDouble(richtungskorrigiert);
        min.ifPresent(aDouble -> rK.set(Math.max(aDouble, rK.get())));
        max.ifPresent(aDouble -> rK.set(Math.min(aDouble, rK.get())));

        return rK.get();
    }

//    public Mat getFinalDedectedGauge() {
//        Mat drawing = Mat.zeros(DEFAULT_SIZE, TYPE);
//        AtomicInteger dist = new AtomicInteger(0);
//        AtomicInteger i = new AtomicInteger(0);
//        labelScale.forEach((rotatedRect, integer) -> {
//            Imgproc.drawMarker(drawing, rotatedRect.center, Helper.WHITE, Imgproc.MARKER_CROSS);
//            Imgproc.putText(drawing, String.valueOf(integer), rotatedRect.center, 0, 1.0, Helper.WHITE);
//            dist.addAndGet((int) Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()));
//            i.incrementAndGet();
//        });
//
//        int r = dist.get() / i.get();
//        Imgproc.arrowedLine(drawing, getCenter(), poolarZuKartesisch(getPointerAngel(), r), Helper.WHITE, 3);
//        Imgproc.putText(drawing, String.valueOf(Math.round(getValue() * 100) / 100.00), poolarZuKartesisch(getPointerAngel(), r / 2), 0, 1.0, Helper.WHITE);
//
//        return drawing;
//    }

    public Mat getDrawing(Mat drawing) {
        if (drawing == null) {
            drawing = Mat.zeros(Gauge.DEFAULT_SIZE, Gauge.TYPE);
        }
        Mat finalDrawing = drawing;
        labelScale.forEach((rotatedRect, aDouble) -> {
            Imgproc.drawMarker(finalDrawing, rotatedRect.center, Helper.WHITE, Imgproc.MARKER_STAR);
            Imgproc.putText(finalDrawing, "" + aDouble, rotatedRect.center, Imgproc.FONT_HERSHEY_DUPLEX, 1.0, Helper.WHITE);
        });

        Imgproc.arrowedLine(finalDrawing, getCenter(), poolarZuKartesisch(getPointerAngel(), getRadius() - 10), Helper.WHITE);

        Imgproc.putText(finalDrawing, "Erkannter Wert: " + Precision.round(getValue(), 2), new Point(0, 30), Imgproc.FONT_HERSHEY_DUPLEX, 1.0, Helper.WHITE);
        return finalDrawing;
    }


//    /*+
//A High-Robust Automatic Reading Algorithm
//of Pointer Meters Based on Text Detection
// */
//    public double getPointerAngelRobustPaper() {
//
//        //Zuerst alles innerhalb der Skalenbeschriftung maskieren
//        AtomicDouble sum = new AtomicDouble(0);
//
//        Mat otsuClone = otsu.clone();
//
//        labelScale.forEach((rotatedRect, integer) -> {
//            double dist = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), rotatedRect.center);
//
//            sum.addAndGet(dist);
//            Helper.drawRotatedRectangle(otsuClone, rotatedRect, Helper.WHITE);
//        });
//
//        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
//        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);
//        Imgproc.circle(mask, getCenter(), (int) sum.get() / labelScale.size(), Helper.BLACK, -1);
////        Imgproc.circle(mask, getCenter(), (int) sum.get(), Helper.BLACK, -1);
//
//
//        //Pixel auf Winkel aufteilen
//        List<Pixel> pixels = Helper.getAllPixel(otsuClone, mask).stream().filter(pixel -> pixel.color == 0).collect(Collectors.toList());
//
//        HashMap<Double, ArrayList<Pixel>> map = new HashMap<>();
//        pixels.forEach(pixel -> {
//            double angle = Precision.round(calculateWinkel(pixel.point), 0) % 360;
//            map.putIfAbsent(angle, new ArrayList<>());
//            map.get(angle).add(pixel);
//
//        });
//
//
//        //Zählern welcher Winkel die meisten Pixel enthält
//        AtomicReference<Double> angle = new AtomicReference<>();
//
//        map.forEach((aDouble, pixels1) -> {
//            if (angle.get() == null || pixels1.size() > map.get(angle.get()).size()) {
//                pixels1.forEach(pixel -> mask.put((int) pixel.point.y, (int) pixel.point.x, pixel.color));
//
//                angle.set(aDouble);
//            }
//        });
//
//        System.out.println("AAAAAAAAAAAAAAAAAAAAA");
//        return angle.get();
//    }

    public Mat getIdealisierteDarstellung() {
        return idealisierteDarstellung.get();
    }


    public Pointer getPointer() {

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<Pointer> zeigerKandidaten = new ArrayList<>();

        for (MatOfPoint contour : contours) {
            Pointer p = new Pointer(contour);

            //Richtung des Zeigers ermitteln
            if (Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()) > Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) {
                p.setArrow(p.getDirection().p1);
                p.setBottom(p.getDirection().p2);
            } else {
                p.setArrow(p.getDirection().p2);
                p.setBottom(p.getDirection().p1);
            }

            if (Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), p.getArrow()) < getRadius()
                    && Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), p.getBottom()) < getRadius() / 3
            ) {
                zeigerKandidaten.add(p);
            }
        }


        //Längsten Kandidaten auswählen
        return zeigerKandidaten.stream().max((o1, o2) -> (int) (o1.getLength() - o2.getLength())).orElse(null);
    }
}
