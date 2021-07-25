package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import lombok.val;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class GaugeOnePointerLearningDataset {
    private final static GaugeOnePointerLearningDataset GAUGE_ONE_POINTER_LEARNING_DATASET = new GaugeOnePointerLearningDataset();

    public static GaugeOnePointerLearningDataset get() {
        return GAUGE_ONE_POINTER_LEARNING_DATASET;
    }

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    private Map<String, Map<Double, Mat>> training = new HashMap<>();

//    @SneakyThrows
//    public static Mat AUFROLLEN(Mat m, double steps) {
//        Mat out = new Mat(new Size(steps, Math.min(m.size().height, m.size().width) / 2), m.type());
//        int nextcol = 0;
//        for (double i = 0; i < 360; i += 360 / steps) {
//            Mat rMap = new Mat();
//            val rotate = Imgproc.getRotationMatrix2D(new Point(m.size().width / 2, m.size().height / 2), i, 1.0);
//            Imgproc.warpAffine(m, rMap, rotate, m.size());
//
//            for (int j = 0; j < m.size().width / 2; j++) {
//                double[] x = rMap.get(j+(int) rMap.size().height / 2,(int) rMap.size().height / 2);
//                out.put((int) rMap.size().height / 2-j, nextcol, x);
//            }
//            nextcol++;
//
//        }
//
//        return out;
//
//    }

    public String generateKey(Size size, double angleSteps) {
        return size.height + "|" + size.width + "|" + angleSteps;
    }

    public Map<Double, Mat> getTrainingset(Size size, double angleSteps) {
        String key = generateKey(size, angleSteps);
        if (!training.containsKey(key)) {
            Mat white = Mat.zeros(size, CvType.CV_8UC3);
            white.setTo(Helper.BLACK);
            Imgproc.circle(white, new Point(size.width / 2, size.height / 2), (int) size.width / 2, BLACK, -1);
            //Zeiger
            Imgproc.line(white, new Point(size.width / 2, size.height / 2), new Point(size.width * 0.9, size.height / 2), WHITE, Math.max((int) (size.height / (180 / (angleSteps * 3))), 2));
            //Gegengewicht
            Imgproc.line(white, new Point(size.width / 2, size.height / 2), new Point((size.width / 2) - (size.width / 6), size.height / 2), WHITE, Math.max((int) (size.height / (180 / (angleSteps * 3))), 2) * 4);

//            HighGui.imshow("", white);
//            HighGui.waitKey();

            HashMap<Double, Mat> map = new HashMap<>();
            for (double i = 0; i < 360; i += angleSteps) {
                Mat dstW = new Mat();
                val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
                Imgproc.warpAffine(white, dstW, rotate, size);
                map.put(i, dstW);
            }

            training.put(key, map);

        }
        return training.get(key);
    }

}


