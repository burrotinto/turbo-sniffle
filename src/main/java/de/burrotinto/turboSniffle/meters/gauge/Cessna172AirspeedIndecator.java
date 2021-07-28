package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.CessnaSpeedTraingSet;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Cessna172AirspeedIndecator extends GaugeOnePointerAutoScale {
    Cessna172AirspeedIndecator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        super(gauge, textDedection, Optional.ofNullable(20.0), Optional.ofNullable(40.0), Optional.ofNullable(200.0), CessnaSpeedTraingSet.get());
    }

    @Override
    public boolean addToScaleMark(RotatedRect rect, Double scale) {
        super.addToScaleMark(rect,scale);
        //Check ob im bereich des TAS
        Double durchschnitt = Helper.durchschnittsDistanz(labelScale.keySet().stream().map(rotatedRect -> rotatedRect.center).collect(Collectors.toList()), getCenter());
        List<Map.Entry<RotatedRect, Double>> prop = new ArrayList<>(labelScale.entrySet());
        for (int i = 0; i < prop.size(); i++) {
            if (Helper.calculateDistanceBetweenPointsWithPoint2D(prop.get(i).getKey().center, getCenter()) > durchschnitt * 1.1) {
                labelScale.remove(prop.get(i).getKey());
            }
        }
        return true;
    }

    @Override
    public void autosetMinMaxMiddle() throws NotGaugeWithPointerException {
        super.autosetMinMaxMiddle();


//        if (!labelScale.values().contains(200.0)) {
//            addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(130.0, getRadius()), new Size(10, 10), 0), getMax().get());
//        }
//
//        double maxW = bildkoordinatenZuPoolar(labelScale.entrySet().stream()
//                .filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue().equals(getMax().get()))
//                .collect(Collectors.toList()).get(0).getKey().center);
//
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 70, getRadius()), new Size(10, 10), 0), getMin().get());
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 118, getRadius()), new Size(10, 10), 0), 60.0);
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW + 205, getRadius()), new Size(10, 10), 0), 80.0);
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW + 160, getRadius()), new Size(10, 10), 0), 100.0);
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW + 120, getRadius()), new Size(10, 10), 0), 120.0);
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW + 85, getRadius()), new Size(10, 10), 0), 140.0);
//        addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW + 50, getRadius()), new Size(10, 10), 0), 160.0);

    }
}
