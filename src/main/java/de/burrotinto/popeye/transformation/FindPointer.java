package de.burrotinto.popeye.transformation;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;


public class FindPointer {
    public Mat getHoughTransform(Mat image, double rho, double theta, int threshold) {
        Mat result = image.clone();
        Mat lines = new Mat();
        Imgproc.HoughLines(image, lines, rho, theta, threshold);

        for (int i = 0; i < lines.cols(); i++) {
            double data[] = lines.get(i, 0);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta * rho1;
            double y0 = sinTheta * rho1;
            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
            Imgproc.line(result, pt1, pt2, new Scalar(255, 0, 0), 2);
        }
        return result;
    }

    public Mat getHoughPTransform(Mat image, double rho, double theta, int threshold) {
        Mat result = image.clone();
        Imgproc.Canny(result, result, 100, 300, 3, false);
        Mat lines = new Mat();
        Imgproc.HoughLinesP(result, lines, rho, theta, threshold);

        for (int i = 0; i < lines.cols(); i++) {
            double[] val = lines.get(0, i);
            Imgproc.line(result, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255, 0, 0), 10);
        }
        return result;
    }

    public Mat getLineSegmentDetector(Mat image, double rho, double theta, int threshold) {
        Mat result = image.clone();
        Imgproc.Canny(result, result, 100, 300, 3, false);
        Mat lines = new Mat();
        Mat x = new Mat(result.size(),result.type());
        Imgproc.createLineSegmentDetector().detect(result,lines);
        Imgproc.createLineSegmentDetector().drawSegments(x,lines);

        for (int i = 0; i < lines.cols(); i++) {
            double[] val = lines.get(0, i);
            Imgproc.line(result, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255, 0, 0), 10);
        }
        return x;
    }

}
