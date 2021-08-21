package de.burrotinto.turboSniffle.meters.gauge.trainingSets;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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

    private final Map<String, List<Pair<Mat, Double[]>>> training = new HashMap<>();


    public List<Pair<Mat, Double[]>> getTrainingset(Size size, double angleSteps) {
        String key = generateKey(size, angleSteps);
        if (!training.containsKey(key)) {
            Point center = new Point(size.width / 2, size.height / 2);
            Mat white = Mat.zeros(size, Gauge.TYPE);
            white.setTo(Helper.BLACK);
            Imgproc.circle(white, center, (int) size.width / 2, BLACK, -1);

            //Zeiger klein Dick
            int breiteDickerZeiger = Math.max((int) (size.height / (180 / (angleSteps * 2))), 2);
            int breiteDuennerZeiger = breiteDickerZeiger / 2;
            Imgproc.line(white, center, new Point(size.width * 0.5, size.height / 2), WHITE, breiteDickerZeiger);

            List<Pair<Mat, Double[]>> pairs = new ArrayList<>();

            for (double i = 0; i < 360; i += angleSteps) {
                Mat dstW = new Mat();
                val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(white, dstW, rotate, size);
                for (double j = 0; j < 360; j += angleSteps) {
                    Mat clone = dstW.clone();
                    Helper.drawLineInMat(clone, center, Math.min(size.width, size.height) * 0.9, j, WHITE, breiteDuennerZeiger);
                    pairs.add(new Pair<>(dstW, new Double[]{i, j}));
                }


            }

            training.put(key, pairs);

        }
        return training.get(key);
    }
}


