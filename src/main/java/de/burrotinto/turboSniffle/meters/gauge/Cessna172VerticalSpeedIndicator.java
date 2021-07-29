package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.CessnaSpeedTraingSet;
import org.opencv.core.RotatedRect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Cessna172VerticalSpeedIndicator extends GaugeOnePointerAutoScale {
    // Value / Winkeldelta zu 200er
    private Map<Double, Double> idealScaleMarks = new HashMap<>();

    Cessna172VerticalSpeedIndicator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        super(gauge, textDedection, Optional.of(20.0), Optional.empty(), Optional.empty(), CessnaSpeedTraingSet.get());
    }


    @Override
    public void autosetMinMaxMiddle() {
        super.autosetMinMaxMiddle();
        addDummyToScaleMark(0.0, 20.0);
        getAngelOfScaleMarkValue(20.0).ifPresent(aDouble -> addDummyToScaleMark((aDouble + 180.0) % 360, 0.0));
        ((HashMap<RotatedRect, Double>) labelScale.clone()).entrySet().stream()
                .filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue() > 20.0 || rotatedRectDoubleEntry.getValue() < 0)
                .forEach(rotatedRectDoubleEntry -> labelScale.remove(rotatedRectDoubleEntry.getKey()));
    }

    @Override
    public double getValue(double angle) {
        double value = super.getValue(angle) + (Math.abs(getAngelOfScaleMarkValue(0.0).get()-angle)/180 );
        if (angle > 180) {
            return Math.max(-value, -20.0);
        } else {
            return Math.min(value, 20.0);
        }
    }
}
