package de.burrotinto.turboSniffle.meters.gauge.trainingSets;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import lombok.val;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CessnaSpeedTraingSet extends TrainingSet {
    private final static CessnaSpeedTraingSet CESSNA_SPEED_TRAING_SET = new CessnaSpeedTraingSet();

    public static CessnaSpeedTraingSet get() {
        return CESSNA_SPEED_TRAING_SET;
    }

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    private final Map<String, List<Pair<Mat, Double>>> training = new HashMap<>();



    public List<Pair<Mat, Double>> getTrainingset(Size size, double angleSteps) {
        String key = generateKey(size, angleSteps);
        if (!training.containsKey(key)) {
            Mat white = Mat.zeros(size, Gauge.TYPE);
            white.setTo(Helper.BLACK);
            //Zeiger
            Imgproc.line(white, new Point(size.width / 2, size.height / 2), new Point(size.width, size.height / 2), WHITE, Math.max((int) (size.height / (180 / (angleSteps * 2))), 2));


            List<Pair<Mat, Double>> pairs = new ArrayList<>();

            for (double i = 0; i < 360; i += angleSteps) {
                Mat dstW = new Mat();
                val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(white, dstW, rotate, size);
                pairs.add(new Pair<>(dstW,i));
            }

            training.put(key, pairs);

        }
        return training.get(key);
    }

}


