package de.burrotinto.turboSniffle;


import de.burrotinto.turboSniffle.cv.DisplayMainFrame;
import de.burrotinto.turboSniffle.meters.MeasuringType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

class SixPackTest{

    public static void main(String[] args) {
        // Load the native OpenCV library
        nu.pattern.OpenCV.loadLocally();

        Mat src = Imgcodecs.imread("data/example/sixpack.jpg");
        var displayMainFrame = new DisplayMainFrame();
        displayMainFrame.setMainframe(src);

//        HighGui.imshow("TEST", displayMainFrame.getRasterdDisplayMainFrame(50));

        displayMainFrame.addMeasuring(new Point(50,50),400,"AIRSPEED", MeasuringType.AnalogSimple,"mph");
//        displayMainFrame.addMeasuring(new Point(400,50),400,"HORIZONT", MeasuringType.AnalogSimple,"");
//        displayMainFrame.addMeasuring(new Point(750,50),400,"ALT", MeasuringType.AnalogSimple,"*100 feet");
//        displayMainFrame.addMeasuring(new Point(50,400),400,"TURN_COORDINATOR", MeasuringType.AnalogSimple,"");
//        displayMainFrame.addMeasuring(new Point(400,400),400,"COMPASS", MeasuringType.AnalogSimple,"°");
        displayMainFrame.addMeasuring(new Point(750,400),400,"VERTICAL_SPEED", MeasuringType.Vertical_Speed,"1000 ft per min");

        System.out.println(displayMainFrame.getValue("AIRSPEED") + displayMainFrame.getUnit("AIRSPEED") );
        System.out.println(displayMainFrame.getValue("VERTICAL_SPEED") + displayMainFrame.getUnit("VERTICAL_SPEED") );

        HighGui.imshow("AIRSPEED",displayMainFrame.getMeasuring("AIRSPEED").getDrawing());
        HighGui.imshow("VERTICAL_SPEED",displayMainFrame.getMeasuring("VERTICAL_SPEED").getDrawing());
        HighGui.waitKey();



    }
}
