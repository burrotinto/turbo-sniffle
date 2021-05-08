package de.burrotinto.popeye.transformation;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Component
public class RectangleExtractor {
    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV = 1;

    public List<Mat> getAllRectangels(Mat src) {
        Mat prepared = new Mat();
        Mat rectangles = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        LinkedList<Mat> listOfRectangles = new LinkedList<>();

        Mat dst = new Mat(), cdst = new Mat(), cdstP;

        //https://docs.opencv.org/3.4/d9/db0/tutorial_hough_lines.html
        // Edge detection
        Imgproc.Canny(src, dst, 50, 200, 3, false);
        // Copy edges to the images that will display the results in BGR
        Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
        cdstP = cdst.clone();
        // Standard Hough Line Transform
        Mat lines = new Mat(); // will hold the results of the detection
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150); // runs the actual detection
        // Draw the lines
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
            Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
            Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }
        // Probabilistic Line Transform
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(dst, linesP, 1, Math.PI/180, 50, 50, 10); // runs the actual detection
        // Draw the lines
        for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            Imgproc.line(cdstP, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }
        // Show results
        HighGui.imshow("Source", src);
        HighGui.imshow("Detected Lines (in red) - Standard Hough Line Transform", cdst);
        HighGui.imshow("Detected Lines (in red) - Probabilistic Line Transform", cdstP);
        // Wait and Exit
        HighGui.waitKey();
        System.exit(0);

        return listOfRectangles;
    }

    public Mat prepareMat(Mat src) {
        Mat dest = new Mat();
        Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(src, dest, 50, 200, 3, false);
//
//        Imgproc.GaussianBlur(dest, dest, new Size(3, 3), 0);
//        Imgproc.Sobel(dest, dest, -1, 1, 0);
//        Imgproc.threshold(dest, dest, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);
//        Imgproc.adaptiveThreshold(dest, dest, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 75, 35);

        return dest;
    }

    public Mat prepareForOCR(Mat src) {
        Mat dest = new Mat();
        Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(dest, dest, new Size(3, 3), 0);
//        Imgproc.Sobel(dest, dest, -1, 1, 0);
//        Imgproc.threshold(dest, dest, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);
//        Imgproc.adaptiveThreshold(dest, dest, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 75, 35);
//        Imgproc.adaptiveThreshold(dest, dest, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 99, 4);
        return dest;
    }
}
