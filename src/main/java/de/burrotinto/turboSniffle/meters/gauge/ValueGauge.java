package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.TrainingSet;
import lombok.Getter;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValueGauge extends AutoEncoderGauge {
    @Getter
    private Optional<Double> skalemarkSteps, min, max;

    ValueGauge(Gauge gauge) throws NotGaugeWithPointerException {
        super(gauge);
    }

    ValueGauge(Gauge gauge, TrainingSet trainingSet) throws NotGaugeWithPointerException {
        super(gauge, trainingSet, 9);
        this.skalemarkSteps = Optional.empty();
        this.min = Optional.empty();
        this.max = Optional.empty();
    }

    ValueGauge(Gauge gauge, Optional<Double> steps, Optional<Double> min, Optional<Double> max, TrainingSet trainingSet, int hiddenLayer) throws NotGaugeWithPointerException {
        super(gauge, trainingSet, hiddenLayer);

        this.skalemarkSteps = steps;
        this.min = min;
        this.max = max;
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
                && !labelScale.values().contains(scale)
        ) {
            labelScale.put(rect, scale);
            return true;
        }
        return false;
    }

    public void autosetMinMaxMiddle() {
        //Check ob über MIN/MAX etwas ermittelt werden kann
        if (labelScale.size() <= 1) {
            if (getMin().isPresent() && getMax().isPresent()) {
                addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(225, getRadius()), new Size(10, 10), 0), getMin().get());
                addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(315, getRadius()), new Size(10, 10), 0), getMax().get());
                addToScaleMarkFORCE(new RotatedRect(poolarZuBildkoordinaten(135, getRadius()), new Size(10, 10), 0), (getMax().get() + getMin().get()) / 3); //Kann SEIN Das WERT nicht EXISTIERT
                addToScaleMarkFORCE(new RotatedRect(poolarZuBildkoordinaten(45, getRadius()), new Size(10, 10), 0), (getMax().get() + getMin().get()) * 2 / 3); //Kann SEIN Das WERT nicht EXISTIERT
            } else {
                //Keine Möglichkeit etwas zu generieren
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


    public void addDummyToScaleMark(double angle, double scale) {
        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(angle, getRadius()), new Size(1, 1), 0), scale);
    }

    public void addDummyToScaleMarkFORCE(double angle, double scale) {
        addToScaleMarkFORCE(new RotatedRect(poolarZuBildkoordinaten(angle, getRadius()), new Size(1, 1), 0), scale);
    }

    public boolean addToScaleMarkFORCE(RotatedRect rect, Double scale) {
        labelScale.put(rect, scale);
        return true;
    }

    public Optional<Double> getAngelOfScaleMarkValue(Double scale) {
        return labelScale.entrySet().stream().filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue().equals(scale)).map(rotatedRectDoubleEntry -> bildkoordinatenZuPoolar(rotatedRectDoubleEntry.getKey().center)).findFirst();
    }


    public double getValue(double angle) {
        autosetMinMaxMiddle();

        if (labelScale.size() < 2) {
            return Double.NaN;
        }

        LinkedList<Pair<Double, Map.Entry<RotatedRect, Double>>> pairs = new LinkedList<>();
        labelScale.entrySet().stream().forEach(e -> {
            Double x = Math.abs(bildkoordinatenZuPoolar(e.getKey().center) - angle);
            pairs.add(new Pair<>(x, e));
            pairs.add(new Pair<>(360 - x, e));
        });

        //Sortierung nach entfernung zum Zeiger
        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));


        Map.Entry<RotatedRect, Double> next = pairs.get(0).p2;

        //Anzeigewerttechnisch näherster am ersten.
        Map.Entry<RotatedRect, Double> next2 = null;

        for (int i = 0; i < pairs.size(); i++) {
            if (pairs.get(i).p2.getValue() != next.getValue()) {
                if (next2 == null || Math.abs(next.getValue() - next2.getValue()) > Math.abs(next.getValue() - pairs.get(i).p2.getValue())) {
                    next2 = pairs.get(i).p2;
                }
            }
        }

        double deltaV = Math.min(Math.abs(bildkoordinatenZuPoolar(next.getKey().center) - bildkoordinatenZuPoolar(next2.getKey().center)), 360 - Math.abs(bildkoordinatenZuPoolar(next.getKey().center) - bildkoordinatenZuPoolar(next2.getKey().center)));

        //Berechnen der Wertes pro Grad
        double xPPDelta = Math.abs(next.getValue() - next2.getValue()) / deltaV;


        //Bestimmen ob der Zeiger innerhalb oder ausserhalb des Bereiches ist
        double summeDerAbstaende = pairs.get(0).p1 + pairs.get(1).p1;
        double value = 0;
        if (Math.abs(summeDerAbstaende - deltaV) <= 0.1) {
            //Fall 1 Zeiger Innerhalb des Bereiches

            //Interpolation je nachdem ob auf oder absteigend
            if (next.getValue() > next2.getValue()) {
                value = next.getValue() - (pairs.get(0).p1 * xPPDelta);
            } else {
                value = next.getValue() + (pairs.get(0).p1 * xPPDelta);
            }

        } else {
            // Fall 2 Zeiger außerhalb des Bereiches
            if (next.getValue() > next2.getValue()) {
                value = next.getValue() + (pairs.get(0).p1 * xPPDelta);
            } else {
                value = next.getValue() - (pairs.get(0).p1 * xPPDelta);
            }
        }

        return value;

    }

//    public double getValue(double angle) {
//        autosetMinMaxMiddle();
//
//        if (labelScale.size() < 2) {
//            return Double.NaN;
//        }
//
//        LinkedList<Pair<Double, Map.Entry<RotatedRect, Double>>> pairs = new LinkedList<>();
//        labelScale.entrySet().stream().forEach(e -> {
//            Double x = Math.abs(bildkoordinatenZuPoolar(e.getKey().center) - angle);
//            pairs.add(new Pair<>(x, e));
//            pairs.add(new Pair<>(360 - x, e));
//        });
//
//        //Sortierung nach entfernung zum Zeiger
//        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));
//
//
//        Map.Entry<RotatedRect, Double> mark1 = pairs.get(0).p2;
//        Map.Entry<RotatedRect, Double> mark2 = pairs.get(1).p2;
//
//        double delta = Math.abs(bildkoordinatenZuPoolar(mark1.getKey().center) - bildkoordinatenZuPoolar(mark2.getKey().center));
//        double deltaMax = Math.max(360 - delta, delta);
//        double deltaMin = 360 - deltaMax;
//
//        //Berechnen der Wertes pro Grad
//        double xPPDeltaMin = Math.abs(mark1.getValue() - mark2.getValue()) / deltaMin;
//
//        //Bestimmen ob der Zeiger innerhalb oder ausserhalb des Bereiches ist
//        double summeDerAbstaende = pairs.get(0).p1 + pairs.get(1).p1;
//        double value = 0;
//        if (Math.abs(summeDerAbstaende - deltaMin) <= 0.1) {
//            //Fall 1 Zeiger Innerhalb des Bereiches
//
//            //Interpolation je nachdem ob auf oder absteigend
//            if (mark1.getValue() > mark2.getValue()) {
//                value = mark1.getValue() - (pairs.get(0).p1 * xPPDeltaMin);
//            } else {
//                value = mark1.getValue() + (pairs.get(0).p1 * xPPDeltaMin);
//            }
//
//        } else {
//            // Fall 2 Zeiger außerhalb des Bereiches
//            if (mark1.getValue() > mark2.getValue()) {
//                value = mark1.getValue() + (pairs.get(0).p1 * xPPDeltaMin);
//            } else {
//                value = mark1.getValue() - (pairs.get(0).p1 * xPPDeltaMin);
//            }
//        }
//        return value;
//    }


    public double getValue() {
        return getValue(getPointerAngel()[getPointerAngel().length - 1]);
    }

    public double getValue(int pointerNumber) {
        return getValue(getPointerAngel()[pointerNumber]);
    }

    /**
     * Malt die erkannten Skalenmarkierungen und den Zeiger auf das Eingabe Mat
     *
     * @param drawing
     * @return
     */
    public Mat getDrawing(Mat drawing) {
        if (drawing == null) {
            drawing = Mat.zeros(Gauge.DEFAULT_SIZE, Gauge.TYPE);
        }
        Mat finalDrawing = drawing;
        Imgproc.cvtColor(drawing, drawing, Imgproc.COLOR_GRAY2RGB);


        Imgproc.putText(finalDrawing, "" + Precision.round(getValue(), 4), new Point(10, 10), Imgproc.FONT_HERSHEY_DUPLEX, 0.5, new Scalar(0, 69, 255));

        labelScale.forEach((rotatedRect, aDouble) -> {
            //Automatisch generierte Punkte Sollen anders Mrkiert werden
            if (Math.abs(Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()) - getRadius()) <= Gauge.DEFAULT_SIZE.width / 10) {
                Imgproc.drawMarker(finalDrawing, rotatedRect.center, new Scalar(0, 69, 255), Imgproc.MARKER_CROSS);
                Imgproc.putText(finalDrawing, "(" + aDouble + ")", rotatedRect.center, Imgproc.FONT_HERSHEY_DUPLEX, 0.5, new Scalar(0, 69, 255));
            } else {
                Imgproc.drawMarker(finalDrawing, rotatedRect.center, new Scalar(0, 69, 255), Imgproc.MARKER_STAR);
                Imgproc.putText(finalDrawing, "" + aDouble, rotatedRect.center, Imgproc.FONT_HERSHEY_DUPLEX, 0.5, new Scalar(0, 69, 255));
            }
        });

        for (int i = 0; i < getPointerAngel().length; i++) {
            Imgproc.arrowedLine(finalDrawing, getCenter(), poolarZuBildkoordinaten(getPointerAngel()[i], (getRadius() - 10) / (i + 1)), new Scalar(0, 69, 255), 5);

        }

        return finalDrawing;
    }

}
