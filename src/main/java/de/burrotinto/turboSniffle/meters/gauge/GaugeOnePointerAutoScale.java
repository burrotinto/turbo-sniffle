package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.GaugeOnePointerLearningDataset;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.TrainingSet;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class GaugeOnePointerAutoScale extends AutoEncoderGauge {

    private final TextDedection textDedection;

    GaugeOnePointerAutoScale(Gauge gauge, TextDedection textDedection, Optional<Double> steps, Optional<Double> min, Optional<Double> max) throws NotGaugeWithPointerException {
        this(gauge, textDedection, steps, min, max, GaugeOnePointerLearningDataset.get());
    }

    GaugeOnePointerAutoScale(Gauge gauge, TextDedection textDedection, Optional<Double> steps, Optional<Double> min, Optional<Double> max, TrainingSet trainingSet) throws NotGaugeWithPointerException {
        super(gauge, steps, min, max, trainingSet);
        this.textDedection = textDedection;
        doOCR();
    }

    protected void doOCR(){
        Mat ideal = otsu.clone();
        // Beschriftung erkennung
        try {
            val textAreas = textDedection.getTextAreas(otsu);
            textAreas.addAll(textDedection.getTextAreas(source));

            val clustered = DistanceToPointClusterer.extract(textAreas, getCenter(), 60, 2);

            //Alle erkannten Textfelder die sich in der äusseren Hälfte befinden
//            for (RotatedRect r : textAreas.stream().filter(rotatedRect -> Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()) >= getRadius() / 2).collect(Collectors.toList())) {
//
            for (RotatedRect r : clustered) {
                try {
                    BufferedImage sub = Helper.Mat2BufferedImage(otsu.submat(r.boundingRect()));
                    String str = textDedection.doOCRNumbers(sub).replaceAll("[\\D.]", "");
                    Double i = Double.parseDouble(str);
                    if (addToScaleMark(r, i)) {
                        System.out.println("OTSU_OCR =" + i + "; Winkel=" + bildkoordinatenZuPoolar(r.center));
                        Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                    }
                } catch (Exception e) {
                    try {
                        BufferedImage sub2 = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
                        String str2 = textDedection.doOCRNumbers(sub2).replaceAll("[\\D.]", "");
                        Double i2 = Double.parseDouble(str2);
                        if (addToScaleMark(r, i2)) {
                            System.out.println("GREYSCAKLE_OCR=" + i2 + "; Winkel=" + bildkoordinatenZuPoolar(r.center));
                            Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                        }
                    } catch (Exception e2) {
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setIdealisierteDarstellung(ideal);
    }

    @Override
    public void autosetMinMaxMiddle() {

    }
}
