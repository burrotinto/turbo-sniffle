package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.gauge.trainingSets.CessnaSpeedTraingSet;

public class Cessna172VerticalSpeedIndicator extends ValueGauge {

    private double twenty = 0;

    Cessna172VerticalSpeedIndicator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        //Nur der 20er ist von interesse
//        super(gauge, textDedection, Optional.of(20.), Optional.of(20.0), Optional.of(20.0), CessnaSpeedTraingSet.get());
//        super(gauge, textDedection, Optional.of(5.0), Optional.of(-20.0), Optional.of(20.0), CessnaSpeedTraingSet.get());
        super(gauge, CessnaSpeedTraingSet.get());
    }

//    @Override
//    protected Mat getOCROptimiert() {
////        Mat ocr = otsu.clone();
////        Core.bitwise_not(Helper.erode(ocr,Imgproc.CV_SHAPE_RECT, 1),ocr);
////        return ocr;
//        return Mat.zeros(otsu.size(),otsu.type());
//    }

    @Override
    public void autosetMinMaxMiddle() {
        addDummyToScaleMark(180, 0);
        addDummyToScaleMark(145, 5);
        addDummyToScaleMark(215, -5);
        addDummyToScaleMark(100, 10);
        addDummyToScaleMark(260, -10);
        addDummyToScaleMark(45, 15);
        addDummyToScaleMark(315, -15);
        addDummyToScaleMark(5, 20);
        addDummyToScaleMark(355, -20);
    }

    @Override
    public double getValue(double angle) {
//        System.out.println(angle);
        return super.getValue(angle) * 100;
//        double xPP = 40 / 350.0;
//        double delta = angle - twenty;
//        double value = 20 - (delta*xPP);
//
//        return Precision.round(value * 100, -2);

//        double value = super.getValue(angle);
//        if (angle > getAngelOfScaleMarkValue(0.0).get()) {
//            return Math.max(-value, -20.0) * 100;
//        } else if (angle <= getAngelOfScaleMarkValue(0.0).get()) {
//            return Math.min(value, 20.0) * 100;
//        } else {
//            return Double.NaN;
//        }
    }
}
