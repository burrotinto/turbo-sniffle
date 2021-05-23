package de.burrotinto.turboSniffle.cessna172;

import de.burrotinto.turboSniffle.booleanAutoEncoder.BooleanAutoencoder;
import de.burrotinto.turboSniffle.cv.DisplayMainFrame;
import de.burrotinto.turboSniffle.meters.MeasuringType;
import de.burrotinto.turboSniffle.meters.gauge.CirceGaugeOnePointer;
import de.burrotinto.turboSniffle.meters.gauge.CircleGauge;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LearnAutoencoder {
    private final static double STEPS = 1;
    private final static String AIRSPEED_FILE = "pointer.png";
    private final static Size SIZE = new Size(512, 512);
    private final BooleanAutoencoder autoencoder = new BooleanAutoencoder();
    private Mat src;


    public LearnAutoencoder() {
        src = Imgcodecs.imread(AIRSPEED_FILE);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);


        LinkedList<Mat> list = new LinkedList<>();

//
//        for (int i = 0; i < 360; i++) {
//            Mat dst = Mat.zeros(SIZE,src.type());
//
//            val rotate = Imgproc.getRotationMatrix2D(new Point(SIZE.width / 2, SIZE.height / 2), i, 1.0);
//            Imgproc.warpAffine(src, dst, rotate, SIZE);
//            list.add(dst);
//        }


        val drawing = Mat.zeros(SIZE, src.type());
        Imgproc.line(drawing, new Point(SIZE.width / 2, SIZE.height / 2), new Point(SIZE.width / 2, 20), new Scalar(255, 255, 255), 20);


        for (int i = 0; i < 360*(1/STEPS); i++) {
            Mat dst = Mat.zeros(SIZE, src.type());

            val rotate = Imgproc.getRotationMatrix2D(new Point(SIZE.width / 2, SIZE.height / 2), i*STEPS, 1.0);
            Imgproc.warpAffine(drawing, dst, rotate, SIZE);
            list.add(dst);
        }

        autoencoder.learn(list);





        val s = Imgcodecs.imread("data/example/20210514_061025 - Kopie.jpg");
        val display = new DisplayMainFrame();
        display.setMainframe(s);
        display.addMeasuring(new Point(0,0),(int)s.size().width,"TEST", MeasuringType.AnalogSimple,"");

        val c = Mat.zeros(display.getMeasuring("TEST").getDrawing().size(),display.getMeasuring("TEST").getDrawing().type());

        LinkedList<MatOfPoint> points = new LinkedList<>();
        points.add(((CirceGaugeOnePointer) display.getMeasuring("TEST")).getPointer()[0].getContour());

            Imgproc.drawContours(c,points,0,new Scalar(255,255,255),-1);


        val x = autoencoder.classify(c);




        Mat cannyOutput = new Mat();
        Imgproc.Canny(x, cannyOutput, 130, 130 * 2);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

//        Mat xxx = Imgcodecs.imread("data/example/20210514_061025 - Kopie.jpg");
//        Imgproc.drawContours(xxx, contours, 0, new Scalar(255, 255, 255));

        System.out.printf(x.type() + "");
        HighGui.imshow("src", x);
        HighGui.imshow("", c);
//        HighGui.imshow("finding", xxx);
        HighGui.waitKey();

    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

        new LearnAutoencoder();
    }
}
