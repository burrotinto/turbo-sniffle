package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.gauge.trainingSets.GaugeTwoPointerLearningDataset;
import de.burrotinto.turboSniffle.gauge.trainingSets.TrainingSet;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Optional;

public class TwoPointerValueGauge extends ValueGauge {

    private int rotation;

    TwoPointerValueGauge(Gauge gauge) throws NotGaugeWithPointerException {
        this(gauge, Optional.empty(), Optional.empty(), Optional.empty(), GaugeTwoPointerLearningDataset.get(), 12, 12);
    }

    TwoPointerValueGauge(Gauge gauge, TrainingSet trainingSet, int hiddenLayer, int rotation) throws NotGaugeWithPointerException {
        this(gauge, Optional.empty(), Optional.empty(), Optional.empty(), trainingSet, hiddenLayer, rotation);
    }

    TwoPointerValueGauge(Gauge gauge, Optional<Double> steps, Optional<Double> min, Optional<Double> max, TrainingSet trainingSet, int hiddenLayer, int rotation) throws NotGaugeWithPointerException {
        super(gauge, steps, min, max, trainingSet, hiddenLayer);
        this.rotation = rotation;
    }

    public double getValue(double angle) {
        //Annahme alle Uhren beginnen stehts bei 90° und drehen sich im uhrzeigersinn
        double calcAngel = 360 - ((angle + 270) % 360);
        return (rotation * calcAngel) / 360.0;
    }


    public double getValue() {
        double big = getValue(getPointerAngel()[0]) * 0.1;

        int little =   (int) getValue(getPointerAngel()[1]);

        //Großer und kleiner Zeiger Passen nicht überein!!!
        if(little != (int) (getValue(getPointerAngel()[1]) - big)){
            double delta = little - (getValue(getPointerAngel()[1]) - big);

            //Alles bei einem delta < 0.2 ist nur ein kleiner Winkelfehler und kann so belassen werden
            // größer muss nach unten korrigiert werden
            if(Math.abs(delta) > 0.2){
                little--;
            }
        }
        return little + (getValue(getPointerAngel()[0])/rotation);
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
            drawing = Mat.zeros(DEFAULT_SIZE, TYPE);
        }
        Mat finalDrawing = drawing;
        Imgproc.cvtColor(drawing, drawing, Imgproc.COLOR_GRAY2RGB);

        Imgproc.rectangle(finalDrawing, new Point(0, 0), new Point(getRadius(), 25), Helper.WHITE, -1);
        Imgproc.putText(finalDrawing, "" + Precision.round(getValue(), 2), new Point(10,20), Imgproc.FONT_HERSHEY_DUPLEX, 0.5, new Scalar(0, 69, 255));

        labelScale.forEach((rotatedRect, aDouble) -> {
            //Automatisch generierte Punkte Sollen anders Mrkiert werden
            if (Math.abs(Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()) - getRadius()) <= DEFAULT_SIZE.width / 10) {
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
