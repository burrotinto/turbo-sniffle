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

public class CessnaTurnTrainingDataset extends TrainingSet {
    private final static CessnaTurnTrainingDataset CESSNA_SPEED_TRAING_SET = new CessnaTurnTrainingDataset();

    public static CessnaTurnTrainingDataset get() {
        return CESSNA_SPEED_TRAING_SET;
    }

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    private final Map<String, List<Pair<Mat, double[]>>> training = new HashMap<>();



    public List<Pair<Mat, double[]>> getTrainingset(Size size, int p) {
        String key = generateKey(size, p);
        if (!training.containsKey(key)) {
            Mat white = Mat.zeros(size, Gauge.TYPE);
            white.setTo(Helper.BLACK);

            Imgproc.circle(white, new Point(size.width / 2, size.height / 2), (int) size.width / 2, Helper.WHITE, -1);
            //Zeiger
            Imgproc.line(white, new Point(0, size.height / 2), new Point(size.width , size.height / 2), Helper.BLACK, (int) calcPointerWidth((int) size.height, Math.pow(2, p), 2) );

            List<Pair<Mat, double[]>> pairs = new ArrayList<>();

            double angleSteps = 360 / Math.pow(2, p-1);
            for (double i = 0; i < 360; i += angleSteps) {
                Mat dstW = new Mat();
                val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(white, dstW, rotate, size);
                pairs.add(new Pair<>(dstW,new double[]{i}));

//                Core.bitwise_not(dstW,dstW);
//                pairs.add(new Pair<>(dstW,new double[]{i}));
            }

            training.put(key, pairs);

        }
        return training.get(key);
    }

}


