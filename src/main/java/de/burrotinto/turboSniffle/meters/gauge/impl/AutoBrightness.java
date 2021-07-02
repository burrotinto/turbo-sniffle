package de.burrotinto.turboSniffle.meters.gauge.impl;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

public class AutoBrightness {

    public static void calculateBrigthness(Mat src) {
        for (int i = 0; i < 256; i += 25) {
            Mat binarized = src.clone();
            for (int y = 0; y < binarized.rows(); y++) {
                for (int x = 0; x < binarized.cols(); x++) {
                    double[] c = new double[1];
                    c[0] = binarized.get(y, x)[0] > i ? 255 : 0;
                    binarized.put(y, x, c);
                }
            }
            MatOfKeyPoint keypoints = getKeypoints(binarized);
            System.out.println(i + " " + keypoints.toList().size());
            Features2d.drawKeypoints(binarized, keypoints, binarized);
            HighGui.imshow("" + i, binarized);
        }
    }

    private static MatOfKeyPoint getKeypoints(Mat src) {
        ORB detector = ORB.create();
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        detector.detectAndCompute(src, new Mat(), keypoints, descriptors);
        return keypoints;
    }
}
