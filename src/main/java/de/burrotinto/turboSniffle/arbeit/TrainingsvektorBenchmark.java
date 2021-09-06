package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.GaugeOnePointerLearningDataset;
import de.burrotinto.turboSniffle.meters.gauge.trainingSets.GaugeTwoPointerLearningDataset;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class TrainingsvektorBenchmark implements Arbeit{
    @Override
    public void machDeinDing() {
        long x = 0;
        for (int p = 8; p < 21; p++) {
            for (int i = 0; i < 20; i++) {
                long s = System.currentTimeMillis();
                new GaugeTwoPointerLearningDataset().getTrainingset(Gauge.DEFAULT_SIZE,p);
                x += System.currentTimeMillis() - s;
//                System.out.println(System.currentTimeMillis() - s);
            }
            System.out.println();
            System.out.println(p +" " +x/20 + "ms");
        }

    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        new TrainingsvektorBenchmark().machDeinDing();
    }
}
