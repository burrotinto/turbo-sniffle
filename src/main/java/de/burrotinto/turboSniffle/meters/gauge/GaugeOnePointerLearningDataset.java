package de.burrotinto.turboSniffle.meters.gauge;

import boofcv.struct.flow.ImageFlow;
import de.burrotinto.turboSniffle.booleanAutoEncoder.BooleanAutoencoder;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GaugeOnePointerLearningDataset {
    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

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

    public static Map<Double,Mat> getTrainingset(Size size, double angleSteps) {

        Mat white = Mat.zeros(size, CvType.CV_8UC3);
//        Imgproc.circle(white, new Point(size.width / 2, size.height / 2), (int) size.width / 2, WHITE, -1);
        Imgproc.line(white, new Point(size.width / 2, size.height / 2), new Point(size.width, size.height / 2), WHITE, Math.max((int) (size.height/(180/(angleSteps*2))), 2));

        HashMap<Double,Mat> map = new HashMap<>();
        for (double i = 0; i < 360; i += angleSteps) {
            Mat dstW = new Mat();
            val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
            Imgproc.warpAffine(white, dstW, rotate, size);
            map.put(i,dstW);
        }

        return map;
    }

}


