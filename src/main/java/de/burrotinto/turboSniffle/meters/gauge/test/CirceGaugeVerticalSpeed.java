package de.burrotinto.turboSniffle.meters.gauge.test;

import de.burrotinto.turboSniffle.cv.TextDedection;
import lombok.SneakyThrows;
import org.opencv.core.Mat;

import java.util.Optional;

public class CirceGaugeVerticalSpeed extends CirceGaugeOnePointer {

    private final double maxValue;
    public CirceGaugeVerticalSpeed(Mat src, TextDedection textDedection) {
        this(src, 100, Optional.empty(),textDedection,2.0);
    }

    @SneakyThrows
    public CirceGaugeVerticalSpeed(Mat inputSrc, int threshold, Optional<Integer> steps, TextDedection textDedection, double maxValue) {
        super(inputSrc,threshold,steps,textDedection);
        update(inputSrc);
        this.maxValue = maxValue;
    }


    public double getValue() {
        double pointer = getWinkel();

        double xpp = 1.0/180;
        double x = pointer;
        if(x > 180) {
            x = -(x - 180);
        } else {
            x = 180 - x;
        }
        return xpp * x * maxValue;
    }
}