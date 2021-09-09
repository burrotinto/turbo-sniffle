package de.burrotinto.turboSniffle.meters.gauge.trainingSets;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CessnaKurskreiselTraingSet extends TrainingSet {
    private final static CessnaKurskreiselTraingSet CESSNA_SPEED_TRAING_SET = new CessnaKurskreiselTraingSet();

    public static CessnaKurskreiselTraingSet get() {
        return CESSNA_SPEED_TRAING_SET;
    }

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    private final Map<String, List<Pair<Mat, double[]>>> training = new HashMap<>();



    public List<Pair<Mat, double[]>> getTrainingset(Size size, int p) {
        String key = generateKey(size, p);
        if (!training.containsKey(key)) {
            Mat kurskreisel =  Imgcodecs.imread("data/ae/kurskreisel.png", Imgcodecs.IMREAD_GRAYSCALE);
            Imgproc.threshold(kurskreisel, kurskreisel, 0, 255, Imgproc.THRESH_BINARY);
            Imgproc.warpAffine(kurskreisel, kurskreisel, Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), -90, 1.0), size);

            Mat kurskreiselG1000 =  Imgcodecs.imread("data/ae/g1000Kurskreisel.png", Imgcodecs.IMREAD_GRAYSCALE);
            Imgproc.threshold(kurskreisel, kurskreisel, 0, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(kurskreiselG1000,kurskreiselG1000);


            List<Pair<Mat, double[]>> pairs = new ArrayList<>();

            double angleSteps = 360 / Math.pow(2, p-1);
            for (double i = 0; i < 360; i += angleSteps) {
                Mat dstW = new Mat();
                val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(kurskreisel, dstW, rotate, size);
                pairs.add(new Pair<>(dstW,new double[]{i}));

                Mat dstW2 = new Mat();
                val rotate2 = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(kurskreiselG1000, dstW2, rotate, size);
                pairs.add(new Pair<>(dstW2,new double[]{i}));
            }

            training.put(key, pairs);

        }
        return training.get(key);
    }

}


