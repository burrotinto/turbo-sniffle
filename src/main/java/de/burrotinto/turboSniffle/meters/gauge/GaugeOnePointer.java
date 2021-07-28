package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.GaugeOnePointerLearningDataset;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.TrainingSet;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.math3.util.Precision;
import org.nd4j.linalg.primitives.AtomicDouble;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public abstract class GaugeOnePointer extends Gauge {
    private final double GENAUIGKEIT = 0.2; //Genauigkeit des verwendeten Autoencoders
    private final Size AUTOENCODER_INPUT_SIZE = Gauge.DEFAULT_SIZE; //Eingabeschicht Autoencoder


    protected final HashMap<RotatedRect, Double> labelScale = new HashMap<>();

    private Optional<Double> pointerAngel = Optional.empty();
    private Mat idealisierteDarstellung;

    private boolean isInit = false;
    @Getter
    private Optional<Double> skalemarkSteps, min, max;

    private TrainingSet trainingSet;

    GaugeOnePointer(Gauge gauge) throws NotGaugeWithPointerException {
        this(gauge, Optional.empty(), Optional.empty(), Optional.empty(), GaugeOnePointerLearningDataset.get());
    }

    GaugeOnePointer(Gauge gauge, Optional<Double> steps, Optional<Double> min, Optional<Double> max, TrainingSet trainingSet) throws NotGaugeWithPointerException {
        super(gauge.source, gauge.canny, gauge.otsu);
        this.skalemarkSteps = steps;
        this.min = min;
        this.max = max;

        this.trainingSet = trainingSet;

        setIdealisierteDarstellung(otsu);

    }

    public void autosetMinMaxMiddle() throws NotGaugeWithPointerException {
        if (isInit == false) {
            //Check ob über MIN/MAX etwas ermittelt werden kann
            if (labelScale.size() <= 1) {
                if (getMin().isPresent() && getMax().isPresent()) {
                    addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(225, getRadius()), new Size(10, 10), 0), getMin().get());
                    addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(315, getRadius()), new Size(10, 10), 0), getMax().get());
                    addToScaleMarkFORCE(new RotatedRect(poolarZuBildkoordinaten(90, getRadius()), new Size(10, 10), 0), (getMax().get() + getMin().get()) / 2); //Kann SEIN Das WERT nicht EXISTIERT
                } else {
                    //Keine Möglichkeit etwas zu generieren
                    throw new NotGaugeWithPointerException();
                }
            }

            // Anhand von MAX auf MIN schließen
            if (getMin().isPresent() && getMax().isPresent() && !labelScale.containsValue(getMin().get()) && labelScale.containsValue(getMax().get())) {
                double maxW = bildkoordinatenZuPoolar(labelScale.entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue())).get().getKey().center);
                if (maxW > 0 && maxW < 180) {
                    addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 90, getRadius()), new Size(10, 10), 0), getMin().get());
                } else {
                    addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 90, getRadius()), new Size(10, 10), 0), getMin().get());
                }
            }
        }
        isInit = true;
    }

    protected void setIdealisierteDarstellung(Mat idealisierteDarstellung) {
        this.idealisierteDarstellung = idealisierteDarstellung;
    }


    /**
     * @param rect
     * @param scale
     * @return Wahr wenn die Scalenmarkierung aufgenommen wurde
     */
    public boolean addToScaleMark(RotatedRect rect, Double scale) {
        if (scale.toString().length() < 6 //Maximale Anzahl an Zahlen plus komma
                && (skalemarkSteps.isEmpty() || scale % skalemarkSteps.get() == 0) //Lineare Aufteilung des Messbereichs
                && !labelScale.values().contains(scale) //Nur einmal darf eine Zahl vorkommen
                && (min.isEmpty() || min.get() <= scale) //Darf Min nicht unterschreiten
                && (max.isEmpty() || max.get() >= scale) //Darf Max nicht überschreiten
                && (labelScale.isEmpty() || Helper.minDistance(labelScale.keySet().stream().map(rotatedRect -> rotatedRect.center).collect(Collectors.toList()), rect.center) > Math.max(rect.size.width, rect.size.height)) //Abstand zur nächsten Zahl
        ) {
            labelScale.put(rect, scale);
            return true;
        }
        return false;
    }

    public boolean addToScaleMarkFORCE(RotatedRect rect, Double scale) {
        labelScale.put(rect, scale);
        return true;
    }


    /**
     * Unsupervised learning mit Boolean Autoencoder
     *
     * @return
     */
    public double getPointerAngel() {
        if (pointerAngel.isEmpty()) {


            Pair<Double, Integer> min = null;
            Mat eingangsVektor = new Mat();

            Imgproc.resize(getIdealisierteDarstellung(), eingangsVektor, AUTOENCODER_INPUT_SIZE);

            List<Pair<Mat, Double>> ausgangsVektoren = trainingSet.getTrainingset(AUTOENCODER_INPUT_SIZE, GENAUIGKEIT);
            for (int i = 0; i < ausgangsVektoren.size(); i++) {
                Mat konjunktion = new Mat();
                Core.bitwise_and(eingangsVektor, ausgangsVektoren.get(i).p1, konjunktion);
                int p = Core.countNonZero(konjunktion);
                if (min == null || min.p2 > p) {
                    min = new Pair<>(ausgangsVektoren.get(i).p2, p);
                }
            }

            pointerAngel = Optional.ofNullable(min.p1);
        }
        return pointerAngel.get();

    }


    private List<Pixel> getPixelsForAngle(List<Pixel> pixels, double angle, int nk) {
        return pixels.stream().filter(pixel -> Math.abs(Precision.round(bildkoordinatenZuPoolar(pixel.point), nk) % 360 - Precision.round(angle, nk) % 360) < Math.pow(10, -nk)).collect(Collectors.toList());
    }

    public double getValue(double angle) {


        val min = labelScale.entrySet().stream().min((o1, o2) -> o1.getValue().compareTo(o2.getValue())).get();
        val max = labelScale.entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue())).get();

        double minW = bildkoordinatenZuPoolar(min.getKey().center);
        double maxW = bildkoordinatenZuPoolar(max.getKey().center);

        double xPP = (max.getValue() - min.getValue()) / (Math.abs((minW - maxW) % 360));

        double delta = minW - angle;

        AtomicDouble value = new AtomicDouble(delta * xPP + min.getValue());
        this.min.ifPresent(aDouble -> value.set(Math.max(aDouble, value.get())));
        this.max.ifPresent(aDouble -> value.set(Math.min(aDouble, value.get())));
        return value.get();

    }

    @SneakyThrows
    public double getValue() {
        //Lazy Initialization
        if (!isInit) {
            autosetMinMaxMiddle();
        }

        double pointer = getPointerAngel();

        LinkedList<Pair<Double, Map.Entry<RotatedRect, Double>>> pairs = new LinkedList<>();
        labelScale.entrySet().stream().forEach(e -> {
            Double x = Math.abs(bildkoordinatenZuPoolar(e.getKey().center) - pointer);
            pairs.add(new Pair<>(x, e));
            pairs.add(new Pair<>(360 - x, e));
        });

        //Sortierung nach entfernung zum Zeiger
        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));

        //Berechnen der Wertes pro Grad
        double xPP = Math.abs(pairs.get(0).p2.getValue() - pairs.get(1).p2.getValue()) / (pairs.get(0).p1 + pairs.get(1).p1);

        Double value = pairs.get(0).p2.getValue() + (pairs.get(0).p1 * xPP);

        return value;
    }

    /**
     * Malt die erkannten Skalenmarkierungen und den Zeiger auf das Eingabe MAt
     *
     * @param drawing
     * @return
     */
    public Mat getDrawing(Mat drawing) {
        if (drawing == null) {
            drawing = Mat.zeros(Gauge.DEFAULT_SIZE, Gauge.TYPE);
        }
        Mat finalDrawing = drawing;
        labelScale.forEach((rotatedRect, aDouble) -> {
            //Automatisch generierte Punkte Sollen anders Mrkiert werden
            if (Math.abs(Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()) - getRadius()) <= Gauge.DEFAULT_SIZE.width / 10) {
                Imgproc.drawMarker(finalDrawing, rotatedRect.center, Helper.WHITE, Imgproc.MARKER_CROSS);
                Imgproc.putText(finalDrawing, "(" + aDouble + ")", rotatedRect.center, Imgproc.FONT_HERSHEY_DUPLEX, 0.5, Helper.WHITE);
            } else {
                Imgproc.drawMarker(finalDrawing, rotatedRect.center, Helper.WHITE, Imgproc.MARKER_STAR);
                Imgproc.putText(finalDrawing, "" + aDouble, rotatedRect.center, Imgproc.FONT_HERSHEY_DUPLEX, 0.5, Helper.WHITE);
            }
        });

        Imgproc.arrowedLine(finalDrawing, getCenter(), poolarZuBildkoordinaten(getPointerAngel(), getRadius() - 10), Helper.WHITE);

        Imgproc.putText(finalDrawing, "" + Precision.round(getValue(), 2), getCenter(), Imgproc.FONT_HERSHEY_DUPLEX, 0.5, Helper.WHITE);
        return finalDrawing;
    }

    public Mat getIdealisierteDarstellung() {
        return idealisierteDarstellung;
    }

}
