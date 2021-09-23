package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.gauge.trainingSets.CessnaSpeedTraingSet;

import java.util.Optional;

public class Cessna172TurnIndicator extends GaugeOnePointerAutoScale {
    Cessna172TurnIndicator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        //Nur der 20er ist von interesse
        super(gauge, textDedection, Optional.of(20.0),Optional.of(20.0),Optional.of(20.0), CessnaSpeedTraingSet.get());
    }


    @Override
    public void autosetMinMaxMiddle() {

        addDummyToScaleMark(0.0, 20.0);

        getAngelOfScaleMarkValue(20.0).ifPresent(aDouble -> addDummyToScaleMarkFORCE((aDouble + 180.0) % 360, 0.0));
//        ((HashMap<RotatedRect, Double>) labelScale.clone()).entrySet().stream()
//                .filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue() > 20.0 || rotatedRectDoubleEntry.getValue() < 0)
//                .forEach(rotatedRectDoubleEntry -> labelScale.remove(rotatedRectDoubleEntry.getKey()));
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
