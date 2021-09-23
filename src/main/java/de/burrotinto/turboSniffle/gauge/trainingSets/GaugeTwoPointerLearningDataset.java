package de.burrotinto.turboSniffle.gauge.trainingSets;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.gauge.Gauge;
import lombok.val;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GaugeTwoPointerLearningDataset extends TrainingSet {
    private final static GaugeTwoPointerLearningDataset GAUGE_ONE_POINTER_LEARNING_DATASET = new GaugeTwoPointerLearningDataset();

    public static GaugeTwoPointerLearningDataset get() {
        return GAUGE_ONE_POINTER_LEARNING_DATASET;
    }

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    private final Map<String, List<Pair<Mat, double[]>>> training = new HashMap<>();


    public List<Pair<Mat, double[]>> getTrainingset(Size size, int p) {
        String key = generateKey(size, p);
        if (!training.containsKey(key)) {
            Point center = new Point(size.width / 2, size.height / 2);

            Mat white = Mat.zeros(size, Gauge.TYPE);
            white.setTo(Helper.BLACK);

            //Skalenscheibe
            Imgproc.circle(white, center, (int) size.width / 2, Helper.WHITE, -1);


            //Zeiger klein Dick
            int breiteDuennerZeiger = calcPointerWidth((int) size.height, Math.pow(2, p * 0.5), 1);
            int breiteDickerZeiger = calcPointerWidth((int) size.height, Math.pow(2, p * 0.5), 1);

            int laengeDuennerZeiger = (int) (size.width / 2);
            int laengeDickerZeiger = (laengeDuennerZeiger*3)/5;

            Imgproc.line(white, center, new Point((size.width / 2) + laengeDickerZeiger, size.height / 2), Helper.BLACK, breiteDickerZeiger);

            List<Pair<Mat, double[]>> pairs = new ArrayList<>();

            double angleSteps = 360 / Math.pow(2, (p) * 0.5);
            for (double i = 0; i < 360; i += angleSteps) {
                Mat dstW = new Mat();
                val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(white, dstW, rotate, size);
                for (double j = 0; j < 360; j += angleSteps) {
                    Mat clone = dstW.clone();
                    Helper.drawLineInMat(clone, center, laengeDuennerZeiger, 360 - j, BLACK, breiteDuennerZeiger);
//                    System.out.println("lang=" + j + " kurz=" + i);
//                    HighGui.imshow("", clone);
//                    HighGui.waitKey();
                    pairs.add(new Pair<>(clone, new double[]{j, i}));

                }

            }

            training.put(key, pairs);

        }
        return training.get(key);
    }
}


