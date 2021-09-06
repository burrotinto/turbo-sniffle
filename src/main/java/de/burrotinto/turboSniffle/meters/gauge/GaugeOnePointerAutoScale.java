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
import java.util.HashMap;
import java.util.Optional;

public class GaugeOnePointerAutoScale extends ValueGauge {

    private final TextDedection textDedection;

    GaugeOnePointerAutoScale(Gauge gauge, TextDedection textDedection, Optional<Double> steps, Optional<Double> min, Optional<Double> max) throws NotGaugeWithPointerException {
        this(gauge, textDedection, steps, min, max, GaugeOnePointerLearningDataset.get());
    }

    GaugeOnePointerAutoScale(Gauge gauge, TextDedection textDedection, Optional<Double> steps, Optional<Double> min, Optional<Double> max, TrainingSet trainingSet) throws NotGaugeWithPointerException {
        super(gauge, steps, min, max, trainingSet, 9);
        this.textDedection = textDedection;
        doOCR();
    }

    protected void doOCR() {
        try {
            val textAreas = textDedection.getTextAreas(otsu);
            textAreas.addAll(textDedection.getTextAreas(source));

            HashMap<RotatedRect, Double> areas = new HashMap<>();
            for (RotatedRect r : textAreas) {
                try {
                    BufferedImage sub = Helper.Mat2BufferedImage(otsu.submat(r.boundingRect()));
                    String s = textDedection.doOCRNumbers(sub);
                    System.out.println(s);
                    String str = textDedection.doOCRNumbers(sub).replaceAll("[\\D.]", "");
                    Double i = Double.parseDouble(str);
                    areas.put(r, i);
                } catch (Exception e) {
                    try {
                        BufferedImage sub2 = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
                        String str2 = textDedection.doOCRNumbers(sub2).replaceAll("[\\D.]", "");
                        Double i2 = Double.parseDouble(str2);
                        areas.put(r, i2);

                    } catch (Exception e2) {
                    }
                }
            }

            val clustered = DistanceToPointClusterer.extractWithOutArea(areas.keySet(), getCenter(), 5, 2);

            clustered.forEach(rotatedRect -> {
                addToScaleMark(rotatedRect, areas.get(rotatedRect));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void autosetMinMaxMiddle() {

    }
}
