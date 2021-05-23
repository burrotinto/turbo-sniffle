//package de.burrotinto.turboSniffle;
//
//
//import de.burrotinto.popeye.transformation.CircleExtractor;
//import de.burrotinto.turboSniffle.cv.Helper;
//import de.burrotinto.turboSniffle.cv.DisplayMainFrame;
//import de.burrotinto.turboSniffle.meters.gauge.CirceGaugeOnePointer;
//import de.burrotinto.turboSniffle.meters.gauge.CircleGauge;
//import lombok.val;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.highgui.HighGui;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import javax.swing.*;
//
//class AnalogSimpleTest
////        extends AbstractSwing
//{
//
////
////    public AnalogSimpleTest(Mat src) {
////        super(src);
////    }
////
////    public AnalogSimpleTest(String file) {
////        super(file);
////    }
////
////    @Override
////    protected Mat getDrawing() {
////        AnalogOneArrow analog = new AnalogOneArrow(srcGray, threshold, 10);
////        Mat drawing = Mat.zeros(srcGray.size(), CvType.CV_8UC3);
////
////
////        Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
////        // contour
//////            Imgproc.drawContours(drawing, contours, i, color);
//////            // ellipse
////        Imgproc.ellipse(drawing, analog.getDisplay(), color, 2);
////        // rotated rectangle
////        Imgproc.drawMarker(drawing, analog.getCenter(), color);
////        Imgproc.line(drawing, analog.getPointer().getArrow(), analog.getCenter(), color, 10);
////        Imgproc.circle(drawing, analog.getCenter(), (int) Helper.calculateDistanceBetweenPointsWithPoint2D(analog.getCenter(), analog.getPointer().getArrow()), color);
////
////        Imgproc.drawMarker(drawing, analog.getPointer().getArrow(), randomColor());
////        Imgproc.putText(drawing, analog.getWinkel() + "°", analog.getCenter(), 0, 1, randomColor());
////
////        Imgproc.putText(drawing, (int) analog.getValue() + " C", analog.getCenter(), 0, 1, randomColor());
////        analog.getLabels().entrySet().stream().forEach(entry -> {
////            Imgproc.putText(drawing, entry.getValue().toString(), entry.getKey().center, 0, 0.5, randomColor());
////        });
////
//////        System.out.println(analog.getValue() + "");
////        return drawing;
////    }
//
//    public static void main(String[] args) {
//        // Load the native OpenCV library
//        nu.pattern.OpenCV.loadLocally();
//        // Schedule a job for the event dispatch thread:
//        // creating and showing this application's GUI.
//
//
////        SwingUtilities.invokeLater(new Runnable() {
////            @Override
////            public void run() {
////                new AnalogSimpleTest("data/example/druck2.jpg");
//////                new CircleExtractor().getAllCircles(Imgcodecs.imread("test.jpeg")).forEach(mat -> {
//////                    try {
//////                        new AnalogOneArrow(mat, 130,10);
//////                        new AnalogSimpleTest(mat);
//////                    } catch (Exception e){}
//////                });
//////
////            }
////        });
//
//
////        Mat src = Imgcodecs.imread("data/example/druck.jpg");
////        var displayMainFrame = new DisplayMainFrame();
////        displayMainFrame.setMainframe(src);
////
////        HighGui.imshow("TEST", displayMainFrame.getRasterdDisplayMainFrame(100));
////        HighGui.waitKey();
//
//        Mat src = Imgcodecs.imread("data/example/druck2.jpg");
//        var displayMainFrame = new DisplayMainFrame();
//        displayMainFrame.setMainframe(src);
//
////        HighGui.imshow("TEST", displayMainFrame.getRasterdDisplayMainFrame(100));
//
//        val c = new CirceGaugeOnePointer(displayMainFrame.getGreyMainframe(),200,10);
//        c.update(displayMainFrame.getGreyMainframe());
//        HighGui.imshow("TEST2", c.getDrawing());
//        HighGui.waitKey();
//
//
//
////        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
////        Imgproc.blur(srcGray, srcGray, new Size(3, 3));
////        AnalogOneArrow analog = new AnalogOneArrow(src, 200, 10);
////        System.out.println(analog.getValue());
////        HighGui.imshow("TEST", analog.getDrawing());
////        HighGui.waitKey();
//
//
//    }
//}
