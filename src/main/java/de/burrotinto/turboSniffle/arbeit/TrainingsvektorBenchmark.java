package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.gauge.Gauge;
import de.burrotinto.turboSniffle.gauge.trainingSets.GaugeTwoPointerLearningDataset;

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
