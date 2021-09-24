package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.gauge.trainingSets.CessnaSpeedTraingSet;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.stream.Collectors;

public class Cessna172AirspeedIndecator extends GaugeOnePointerAutoScale {
    private Map<Double, Double> idealScaleMarks = new HashMap<>();

    Cessna172AirspeedIndecator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        super(gauge, textDedection, Optional.ofNullable(20.0), Optional.ofNullable(40.0), Optional.ofNullable(200.0), CessnaSpeedTraingSet.get());

        //Annäherungswerte an Perfekte Skalenscheibe
        idealScaleMarks.put(40.0, -70.0);
        idealScaleMarks.put(60.0, -118.0);
        idealScaleMarks.put(80.0, 205.0);
        idealScaleMarks.put(100.0, 160.0);
        idealScaleMarks.put(120.0, 120.0);
        idealScaleMarks.put(140.0, 85.0);
        idealScaleMarks.put(160.0, 50.0);
        idealScaleMarks.put(180.0, 25.0);
        idealScaleMarks.put(200.0, 0.0);

//        idealisierteDarstellung = otsu.clone();
//        Core.bitwise_not(idealisierteDarstellung,idealisierteDarstellung);

    }

    @Override
    protected Mat getOCROptimiert() {
        Mat ocr = otsu.clone();
        Core.bitwise_not(Helper.erode(ocr, Imgproc.CV_SHAPE_RECT, 1),ocr);
        return ocr;
    }

    @Override
    public boolean addToScaleMark(RotatedRect rect, Double scale) {
        super.addToScaleMark(rect, scale);
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

//    @Override
//    protected void setIdealisierteDarstellung(Mat idealisierteDarstellung) {
//    }

    @Override
    public void autosetMinMaxMiddle() {
        super.autosetMinMaxMiddle();

//        if (!labelScale.isEmpty()) {
//            val value = labelScale.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).findFirst().get();
//            Double winkel = bildkoordinatenZuPoolar(value.getKey().center);
//            super.addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(winkel - idealScaleMarks.get(value.getValue()), getRadius()), new Size(10, 10), 0), 200.0);
//
//            labelScale.entrySet().stream().filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue().equals(200.0)).findFirst().ifPresent(rotatedRectDoubleEntry -> {
//                val zweihundert = bildkoordinatenZuPoolar(value.getKey().center);
//                idealScaleMarks.forEach((v, w) -> super.addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(zweihundert + w, getRadius()), new Size(10, 10), 0), v));
//            });
//
//
//
//        }


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

    @Override
    public double getValue() {
        if(super.getValue() > 200 || super.getValue() < 40){
            return Double.NaN;
        } else {
            return super.getValue();
        }

    }
}
