package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.CessnaKurskreiselTraingSet;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.CessnaSpeedTraingSet;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cessna172KurskreiselIndecator extends ValueGauge {
    private Map<Double, Double> idealScaleMarks = new HashMap<>();

    Cessna172KurskreiselIndecator(Gauge gauge) throws NotGaugeWithPointerException {
        super(gauge, CessnaKurskreiselTraingSet.get());
    }

    @Override
    public double getValue(double angle) {
        return (angle +270) % 360;
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
