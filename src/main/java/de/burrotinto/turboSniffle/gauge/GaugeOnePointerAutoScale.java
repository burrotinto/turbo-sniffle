package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.gauge.trainingSets.GaugeOnePointerLearningDataset;
import de.burrotinto.turboSniffle.gauge.trainingSets.TrainingSet;
import de.burrotinto.turboSniffle.gauge.cluster.DistanceToPointClusterer;
import lombok.val;
import org.opencv.core.RotatedRect;
import org.opencv.highgui.HighGui;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class GaugeOnePointerAutoScale extends ValueGauge {

    protected final TextDedection textDedection;

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
            val textAreas = textDedection.getTextAreas(otsu).stream().filter(rotatedRect -> Math.min(rotatedRect.size.width, rotatedRect.size.height) >= 20).collect(Collectors.toList());
//            textAreas.addAll(textDedection.getTextAreas(source).stream().filter(rotatedRect -> Math.min(rotatedRect.size.width, rotatedRect.size.height) >= 20).collect(Collectors.toList()));

            HashMap<RotatedRect, Double> areas = new HashMap<>();
            for (RotatedRect r : textAreas) {

//                Double d = textDedection.doOCRBruteForceNumber(source.submat(r.boundingRect()));
//                if (!d.isNaN()) {
//                    areas.put(r, d);
//                }
                try {
//                    String s = textDedection.doOCRNumbers(sub);
//                    System.out.println(s);
//
                    HighGui.imshow("aaa",otsu.submat(r.boundingRect()));
                    String str = textDedection.doOCRNumbers(
                            Helper.sharpen(
//                                    Helper.erode(
                                            otsu.submat(r.boundingRect())
//                                            , Imgproc.CV_SHAPE_RECT, 1)
                            )
                    ).replaceAll("[\\D.]", "");

                    Double i = Double.parseDouble(str);
                    areas.put(r, i);
                } catch (Exception e) {
                    try {
//                        String str2 = textDedection.doOCRNumbers(source.submat(r.boundingRect())).replaceAll("[\\D.]", "");
//                        Double i2 = Double.parseDouble(str2);
//                        areas.put(r, i2);

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
