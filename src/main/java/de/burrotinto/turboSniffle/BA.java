package de.burrotinto.turboSniffle;


import de.burrotinto.turboSniffle.cv.DisplayMainFrame;
import de.burrotinto.turboSniffle.meters.MeasuringType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

class BA {

    public static void main(String[] args) {
        // Load the native OpenCV library
        nu.pattern.OpenCV.loadLocally();

        Mat src = Imgcodecs.imread("data/example/temp.jpg");

        DisplayMainFrame displayMainFrame = new DisplayMainFrame();
        displayMainFrame.setMainframe(src);
        displayMainFrame.addMeasuring(new Point(0.0,0.0),src.height(),"Gauge",MeasuringType.AnalogSimple,"C");
        System.out.println(displayMainFrame.getMeasuring("Gauge").getValue()+"");



    }
}
