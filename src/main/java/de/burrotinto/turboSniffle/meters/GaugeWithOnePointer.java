package de.burrotinto.turboSniffle.meters;

import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import org.opencv.core.Mat;

import java.util.Optional;

public class GaugeWithOnePointer extends Gauge {

    private Optional<Double> ponterAngel = Optional.empty();
    public GaugeWithOnePointer(Mat source) {
        super(source);
    }

    public GaugeWithOnePointer(Mat source, Double ponterAngel) {
        super(source);
        this.ponterAngel = Optional.ofNullable(ponterAngel);
    }

    public double getPonterAngel() {
        return ponterAngel.orElse(-1.0);
    }
}
