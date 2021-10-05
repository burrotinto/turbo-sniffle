package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.gauge.cluster.DistanceToPointClusterer;
import de.burrotinto.turboSniffle.gauge.trainingSets.GaugeOnePointerLearningDataset;
import de.burrotinto.turboSniffle.gauge.trainingSets.TrainingSet;
import lombok.val;
import org.opencv.core.Mat;
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
        super(gauge, steps, min, max, trainingSet, 10);
        this.textDedection = textDedection;
        doOCR();
    }

    protected Mat getOCROptimiert (){
        return getIdealisierteDarstellung();
    }

    protected void doOCR() {
        try {
            HighGui.imshow("",getOCROptimiert());
            Mat ocr = getOCROptimiert();
            val textAreas = textDedection.getTextAreas(ocr).stream().filter(rotatedRect -> Math.min(rotatedRect.size.width, rotatedRect.size.height) >= 20).collect(Collectors.toList());
            HashMap<RotatedRect, Double> areas = new HashMap<>();
            for (RotatedRect r : textAreas) {

                try {

                    String str = textDedection.doOCRNumbers(
                            Helper.sharpen(
                                    ocr.submat(r.boundingRect())
                            )
                    ).replaceAll("[\\D.]", "");

                    Double i = Double.parseDouble(str);
                    areas.put(r, i);
                } catch (Exception e) {
                    try {

                    } catch (Exception e2) {
                    }
                }
            }

            val clustered = DistanceToPointClusterer.extractWithOutArea(areas.keySet(), getCenter(), 5, 3);

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
