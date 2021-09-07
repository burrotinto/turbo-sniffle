package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.CessnaSpeedTraingSet;
import lombok.val;
import org.opencv.core.RotatedRect;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class Cessna172VerticalSpeedIndicator extends GaugeOnePointerAutoScale {

    private double twenty = 0;

    Cessna172VerticalSpeedIndicator(Gauge gauge, TextDedection textDedection) throws NotGaugeWithPointerException {
        //Nur der 20er ist von interesse
        super(gauge, textDedection, Optional.of(20.), Optional.of(20.0), Optional.of(20.0), CessnaSpeedTraingSet.get());
    }

    protected void doOCR() {
//        try {
//            val textAreas = textDedection.getTextAreas(otsu);
//            textAreas.addAll(textDedection.getTextAreas(source));
//
//            HashMap<RotatedRect, Double> areas = new HashMap<>();
//            for (RotatedRect r : textAreas) {
//                try {
//                    BufferedImage sub = Helper.Mat2BufferedImage(otsu.submat(r.boundingRect()));
//                    String str = textDedection.doOCRNumbers(sub).replaceAll("[\\D.]", "");
//                    Double i = Double.parseDouble(str);
//                    areas.put(r, i);
//                } catch (Exception e) {
//                    try {
//                        BufferedImage sub2 = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
//                        String str2 = textDedection.doOCRNumbers(sub2).replaceAll("[\\D.]", "");
//                        Double i2 = Double.parseDouble(str2);
//                        areas.put(r, i2);
//
//                    } catch (Exception e2) {
//                    }
//                }
//            }
//            areas.entrySet().stream().filter(rotatedRectDoubleEntry -> rotatedRectDoubleEntry.getValue() == 20.0).findFirst().ifPresent(rotatedRectDoubleEntry -> {
//                twenty = bildkoordinatenZuPoolar(rotatedRectDoubleEntry.getKey().center);
////                addToScaleMark(rotatedRectDoubleEntry.getKey(), rotatedRectDoubleEntry.getValue());
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void autosetMinMaxMiddle() {

//        if (!getAngelOfScaleMarkValue(20.0).isPresent()) {
//            addDummyToScaleMark(0.0, 20.0);
//        }
//
//        getAngelOfScaleMarkValue(20.0).ifPresent(aDouble -> addDummyToScaleMarkFORCE((aDouble + 180.0) % 360, 0.0));
    }

    @Override
    public double getValue(double angle) {
        System.out.println(angle);
        double xPP = 40 / 360.0;
        double delta = angle - twenty;
        double value = 20 - (delta*xPP);

        return value * 100;
//        double value = super.getValue(angle);
//        if (angle > getAngelOfScaleMarkValue(0.0).get()) {
//            return Math.max(-value, -20.0) * 100;
//        } else if (angle <= getAngelOfScaleMarkValue(0.0).get()) {
//            return Math.min(value, 20.0) * 100;
//        } else {
//            return Double.NaN;
//        }
    }

//    public double getValue(double angle) {
//        autosetMinMaxMiddle();
//
//        if (labelScale.size() < 2) {
//            return Double.NaN;
//        }
//
//        LinkedList<Pair<Double, Map.Entry<RotatedRect, Double>>> pairs = new LinkedList<>();
//        labelScale.entrySet().stream().forEach(e -> {
//            Double x = Math.abs(bildkoordinatenZuPoolar(e.getKey().center) - angle);
//            pairs.add(new Pair<>(x, e));
//            pairs.add(new Pair<>(360 - x, e));
//        });
//
//        //Sortierung nach entfernung zum Zeiger
//        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));
//
//
//        Map.Entry<RotatedRect, Double> next = pairs.get(0).p2;
//
//        //Anzeigewerttechnisch näherster am ersten.
//        Map.Entry<RotatedRect, Double> next2 = null;
//
//        for (int i = 0; i < pairs.size(); i++) {
//            if (pairs.get(i).p2.getValue() != next.getValue()) {
//                if (next2 == null || Math.abs(next.getValue() - next2.getValue()) > Math.abs(next.getValue() - pairs.get(i).p2.getValue())) {
//                    next2 = pairs.get(i).p2;
//                }
//            }
//        }
//
//        double deltaV = Math.min(Math.abs(bildkoordinatenZuPoolar(next.getKey().center) - bildkoordinatenZuPoolar(next2.getKey().center)), 360 - Math.abs(bildkoordinatenZuPoolar(next.getKey().center) - bildkoordinatenZuPoolar(next2.getKey().center)));
//
//        //Berechnen der Wertes pro Grad
//        double xPPDelta = Math.abs(next.getValue() - next2.getValue()) / deltaV;
//
//
//        //Bestimmen ob der Zeiger innerhalb oder ausserhalb des Bereiches ist
//        double summeDerAbstaende = pairs.get(0).p1 + pairs.get(1).p1;
//        double value = 0;
//        if (Math.abs(summeDerAbstaende - deltaV) <= 0.1) {
//            //Fall 1 Zeiger Innerhalb des Bereiches
//
//            //Interpolation je nachdem ob auf oder absteigend
//            if (next.getValue() > next2.getValue()) {
//                value = next.getValue() - (pairs.get(0).p1 * xPPDelta);
//            } else {
//                value = next.getValue() + (pairs.get(0).p1 * xPPDelta);
//            }
//
//        } else {
//            // Fall 2 Zeiger außerhalb des Bereiches
//            if (next.getValue() > next2.getValue()) {
//                value = next.getValue() + (pairs.get(0).p1 * xPPDelta);
//            } else {
//                value = next.getValue() - (pairs.get(0).p1 * xPPDelta);
//            }
//        }
//
//        return value;
//
//    }
}
