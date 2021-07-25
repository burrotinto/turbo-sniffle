package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.TextDedection;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

import java.util.Optional;
import java.util.stream.Collectors;

public class Cessna172AirspeedIndecator extends GaugeOnePointerAutoScale {
    Cessna172AirspeedIndecator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        super(gauge, textDedection, Optional.ofNullable(20.0), Optional.ofNullable(40.0), Optional.ofNullable(200.0));
    }

    @Override
    protected void autosetMinMaxMiddle() throws NotGaugeWithPointerException {
        if (labelScale.isEmpty()) {
            addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(130.0, getRadius()), new Size(10, 10), 0), getMax().get());
        }

        double maxW = bildkoordinatenZuPoolar(labelScale.entrySet().stream().filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue() == getMax().get()).collect(Collectors.toList()).get(0).getKey().center);

        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 70, getRadius()), new Size(10, 10), 0), getMin().get());
        addToScaleMarkFORCE(new RotatedRect(poolarZuBildkoordinaten(maxW - 130, getRadius()), new Size(10, 10), 0), 70.0);
        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW + 50, getRadius()), new Size(10, 10), 0), 160.0);

    }
}
