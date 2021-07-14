package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.popeye.transformation.Pair;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.val;
import org.apache.commons.math3.util.Precision;
import org.nd4j.linalg.primitives.AtomicDouble;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class AnalogOnePointer extends Gauge {
    private final int GENAUIGKEIT = 1;

    private final TextDedection textDedection;
    private final HashMap<RotatedRect, Double> labelScale = new HashMap<>();

    private Optional<Double> pointerAngel = Optional.empty();

    AnalogOnePointer(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        super(gauge.source, gauge.canny, gauge.otsu);
        this.textDedection = textDedection;

//        HighGui.imshow("Source", source);
//        HighGui.imshow("Canny", canny);
//        HighGui.imshow("Otsu", otsu);
        // Beschriftung erkennung
        val textAreas = DistanceToPointClusterer.extract(textDedection.getTextAreas(otsu), getCenter(), (int) (DEFAULT_SIZE.height / 20), 2);
//        textAreas.addAll(DistanceToPointClusterer.extract(textDedection.getTextAreas(source), getCenter(), (int) (DEFAULT_SIZE.height / 20), 2));

        Mat drawMat = getSource().clone();


        for (RotatedRect r : textAreas) {
            try {
                Helper.drawRotatedRectangle(drawMat, r, Helper.GREY);
                Imgproc.putText(drawMat, r.angle + "", r.center, 0, 1.0, Helper.WHITE);

                BufferedImage sub = Helper.Mat2BufferedImage(otsu.submat(r.boundingRect()));
                String str = textDedection.doOCRNumbers(sub);
                Double i = Double.parseDouble(str);
                labelScale.put(r, i);
                System.out.println("" + i);
            } catch (Exception e) {
                try {
                    BufferedImage sub2 = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
                    String str2 = textDedection.doOCRNumbers(sub2);
                    Double i2 = Double.parseDouble(str2);
                    labelScale.put(r, i2);
                    System.out.println("2=" + i2);
                } catch (Exception e2) {
                }
            }

        }
        if (labelScale.size() < 1) {
            throw new NotGaugeWithPointerException();
        }
//        HighGui.imshow("DRAW",drawMat);
//        HighGui.waitKey();
    }

    /**
     * Unsupervised learning
     *
     * @return
     */
    public double getPointerAngel() {
        if (pointerAngel.isEmpty()) {
            //Zuerst alles innerhalb der Skalenbeschriftung maskieren
            AtomicDouble sum = new AtomicDouble(0);

            Mat otsuClone = otsu.clone();

            labelScale.forEach((rotatedRect, integer) -> {
                double dist = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), rotatedRect.center);

                sum.addAndGet(dist);
                Helper.drawRotatedRectangle(otsuClone, rotatedRect, Helper.WHITE);
            });

            Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
            Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);
            Imgproc.circle(mask, getCenter(), (int) sum.get() / labelScale.size(), Helper.BLACK, -1);


            //Todo Parallelisieren
            AtomicReference<Pair<Double, Integer>> max = new AtomicReference<>(new Pair<>(0.0, 0));
            GaugeOnePointerLearningDataset.getTrainingset(Gauge.DEFAULT_SIZE, GENAUIGKEIT).forEach((aDouble, mat) -> {
                int p = Helper.countPixel(otsuClone, mat, Helper.BLACK);
                if (max.get().p2 < p) {
                    max.set(new Pair<>(aDouble, p));
                }
            });

            pointerAngel = Optional.ofNullable(max.get().p1);
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
        double x = (minW - pointer);

        return pairs.get(0).p2.getValue() + (x * xPP);
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

        int r = dist.get() / i.get();
        Imgproc.arrowedLine(drawing, getCenter(), poolarZuKartesisch(getPointerAngel(), r), Helper.WHITE, 3);
        Imgproc.putText(drawing, String.valueOf(Math.round(getValue() * 100) / 100.00), poolarZuKartesisch(getPointerAngel(), r / 2), 0, 1.0, Helper.WHITE);

        return drawing;
    }

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

        Imgproc.putText(finalDrawing, "Erkannter Wert: " + getValue(), new Point(0, 30), Imgproc.FONT_HERSHEY_DUPLEX, 1.0, Helper.WHITE);
        return finalDrawing;
    }


    /*+
A High-Robust Automatic Reading Algorithm
of Pointer Meters Based on Text Detection
 */
    public double getPointerAngelRobustPaper() {

        //Zuerst alles innerhalb der Skalenbeschriftung maskieren
        AtomicDouble sum = new AtomicDouble(0);

        Mat otsuClone = otsu.clone();

        labelScale.forEach((rotatedRect, integer) -> {
            double dist = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), rotatedRect.center);

            sum.addAndGet(dist);
            Helper.drawRotatedRectangle(otsuClone, rotatedRect, Helper.WHITE);
        });

        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);
        Imgproc.circle(mask, getCenter(), (int) sum.get() / labelScale.size(), Helper.BLACK, -1);
//        Imgproc.circle(mask, getCenter(), (int) sum.get(), Helper.BLACK, -1);


        //Pixel auf Winkel aufteilen
        List<Pixel> pixels = Helper.getAllPixel(otsuClone, mask).stream().filter(pixel -> pixel.color == 0).collect(Collectors.toList());

        HashMap<Double, ArrayList<Pixel>> map = new HashMap<>();
        pixels.forEach(pixel -> {
            double angle = Precision.round(calculateWinkel(pixel.point), 0) % 360;
            map.putIfAbsent(angle, new ArrayList<>());
            map.get(angle).add(pixel);

        });


        //Zählern welcher Winkel die meisten Pixel enthält
        AtomicReference<Double> angle = new AtomicReference<>();

        map.forEach((aDouble, pixels1) -> {
            if (angle.get() == null || pixels1.size() > map.get(angle.get()).size()) {
                pixels1.forEach(pixel -> mask.put((int) pixel.point.y, (int) pixel.point.x, pixel.color));

                angle.set(aDouble);
            }
        });

        System.out.println("AAAAAAAAAAAAAAAAAAAAA");
        return angle.get();
    }
}
