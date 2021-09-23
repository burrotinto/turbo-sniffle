package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.gauge.trainingSets.GaugeTwoPointerLearningDataset;

import java.util.HashMap;
import java.util.Map;

public class Cessna172AltimeterIndecator extends TwoPointerValueGauge {

    private Map<Double, Double> idealScaleMarks = new HashMap<>();

    Cessna172AltimeterIndecator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        super(gauge, GaugeTwoPointerLearningDataset.get(), 12, 10);
    }

    public double getValue() {
        return super.getValue() * 1000;
    }
}
