//package de.burrotinto.popeye.transformation.examples;
//
//
//import de.burrotinto.popeye.transformation.CircleExtractor;
//import de.burrotinto.turboSniffle.cv.Helper;
//import de.burrotinto.turboSniffle.meters.gauge.AnalogOneArrow;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.RotatedRect;
//import org.opencv.core.Scalar;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import javax.swing.*;
//
//class FittingElipse extends AbstractSwing {
//
//
//    public FittingElipse(Mat src) {
//        super(src);
//    }
//
//    public FittingElipse(String file) {
//        super(file);
//    }
//
//    @Override
//    protected Mat getDrawing() {
//        AnalogOneArrow analog = new AnalogOneArrow(srcGray, threshold, 20);
//        Mat drawing = Mat.zeros(srcGray.size(), CvType.CV_8UC3);
//
//
//        Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
//        // contour
////            Imgproc.drawContours(drawing, contours, i, color);
////            // ellipse
//        Imgproc.ellipse(drawing, analog.getDisplay(), color, 2);
//        // rotated rectangle
//        Imgproc.drawMarker(drawing, analog.getCenter(), color);
//        Imgproc.line(drawing, analog.getPointer().getArrow(), analog.getCenter(), color, 10);
//        Imgproc.circle(drawing, analog.getCenter(), (int) Helper.calculateDistanceBetweenPointsWithPoint2D(analog.getCenter(), analog.getPointer().getArrow()), color);
//        Imgproc.putText(drawing, (int)analog.getValue() + " C", analog.getCenter(), 0, 1, randomColor());
//        Imgproc.drawMarker(drawing, analog.getPointer().getArrow(), randomColor());
////        Imgproc.putText(drawing, analog.getWinkel() + "°", analog.getCenter(), 0, 1, randomColor());
//        analog.getLabels().entrySet().stream().forEach(entry -> {
//            Imgproc.putText(drawing,entry.getValue().toString(),entry.getKey().center,0,0.5,randomColor());
//        });
//
//        System.out.println(analog.getValue()+"");
//        return drawing;
//    }
//
//    public static void main(String[] args) {
//        // Load the native OpenCV library
//        nu.pattern.OpenCV.loadLocally();
//        // Schedule a job for the event dispatch thread:
//        // creating and showing this application's GUI.
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                new FittingElipse("C:\\Users\\fklinger\\nxbt\\studium\\Abschlussarbeit\\turbo-sniffle3\\data\\example\\temp.jpg");
////                new CircleExtractor().getAllCircles(Imgcodecs.imread("C:\\Users\\fklinger\\nxbt\\studium\\Abschlussarbeit\\turbo-sniffle3\\data\\example\\20210514_061025.jpg")).forEach(FittingElipse::new);
//
//            }
//        });
//    }
//}
