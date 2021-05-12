package de.burrotinto.popeye.transformation;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class CircleExtractor {
    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV = 1;

    public List<Mat> getAllCircles(Mat src) {
        Mat prepared = prepareMat(src);
        Mat circles = new Mat();

        LinkedList<Mat> listOfCircles = new LinkedList<>();

        Imgproc.HoughCircles(prepared, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double) prepared.rows() / 16, // change this value to detect circles with different distances to each other
                100.0, 30.0, 60, 100); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);

            //Finde Radius und Mittelpunkt
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);

            //Bildauschnitt
            Mat newImage = src.submat((int) Math.round(center.y - radius), (int) Math.round(center.y + radius), (int) Math.round(center.x - radius), (int) Math.round(center.x + radius));

            Mat resizeimage = new Mat();
            Size sz = new Size(256, 256);
            Imgproc.resize(newImage, resizeimage, sz);

            listOfCircles.add(resizeimage);
        }
        return listOfCircles;
    }

    public Mat prepareMat(Mat src) {
        Mat dest = new Mat();

        Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(dest, dest, 5);


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
