package de.burrotinto.turboSniffle.meters.gauge.trainingSets;

import de.burrotinto.turboSniffle.cv.Pair;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;

public abstract class TrainingSet {
    public String generateKey(Size size, double angleSteps) {
        return size.height + "|" + size.width + "|" + angleSteps;
    }

    public abstract List<Pair<Mat, double[]>> getTrainingset(Size size, int p);

    public int calcPointerWidth(int w, double q, double o) {
        return (int) Math.ceil((w * Math.PI* o)/(q));
    }

    public int calcPointerLength(int w) {
        return (int) Math.ceil((w * 4.0) / 10);
    }
}
