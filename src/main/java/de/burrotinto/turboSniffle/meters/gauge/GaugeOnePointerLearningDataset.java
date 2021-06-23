package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.meters.GaugeWithOnePointer;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GaugeOnePointerLearningDataset {
    private static final Scalar WHITE = new Scalar(255,255,255);
    private static final Scalar BLACK = new Scalar(0,0,0);


    public static List<GaugeWithOnePointer> getTrainingset(Size size, double angleSteps){
        Mat drawing = Mat.zeros(size, CvType.CV_8UC3);
        Imgproc.circle(drawing,new Point(size.width / 2, size.height / 2),(int)size.width/2,WHITE,-1);
        Imgproc.line(drawing, new Point(size.width / 2, size.height / 2), new Point(size.width, size.height/2), BLACK, 10);

        ArrayList<GaugeWithOnePointer> list = new ArrayList<>();
        for (double i = 0; i < 360; i += angleSteps) {
            Mat dst = new Mat();

            val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
            Imgproc.warpAffine(drawing, dst, rotate, size);
            val gauge = new GaugeWithOnePointer(dst,i);
            list.add(gauge);
        }

        return list;
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

        GaugeOnePointerLearningDataset.getTrainingset(new Size(256.0,256.0),45.0).forEach(gaugeWithOnePointer -> {
            HighGui.imshow(gaugeWithOnePointer.getPonterAngel()+"",gaugeWithOnePointer.getSource());
        });
        HighGui.waitKey();
    }
}


