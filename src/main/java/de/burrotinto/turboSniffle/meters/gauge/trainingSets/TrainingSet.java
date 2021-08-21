package de.burrotinto.turboSniffle.meters.gauge.trainingSets;

import de.burrotinto.turboSniffle.cv.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;
import java.util.Map;

public abstract class TrainingSet {
    public String generateKey(Size size, double angleSteps) {
        return size.height + "|" + size.width + "|" + angleSteps;
    }

    public abstract List<Pair<Mat, Double[]>> getTrainingset(Size size, double angleSteps);
}
