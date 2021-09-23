package de.burrotinto.turboSniffle.cv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Helper {
    public static final Scalar WHITE = new Scalar(255, 255, 255);
    public static final Scalar BLACK = new Scalar(0, 0, 0);
    public static final Scalar GREY = new Scalar(128, 128, 128);

    static public double calculateDistanceBetweenPointsWithPoint2D(Point x, Point y) {
        return Point2D.distance(x.x, x.y, y.x, y.y);
    }

    static public double calculateDistanceBetweenPointsWithPoint2D(Pair<Point, Point> pair) {
        return calculateDistanceBetweenPointsWithPoint2D(pair.p1, pair.p2);
    }

    static public double durchschnittsDistanz(List<Point> fromPoints, Point toPoint) {
        double sum = 0;
        for (int i = 0; i < fromPoints.size(); i++) {
            sum += calculateDistanceBetweenPointsWithPoint2D(toPoint, fromPoints.get(i));
        }
        return sum / fromPoints.size();
    }

    static public double maxDistance(List<Point> fromPoints, Point toPoint) {
        double max = 0;
        for (int i = 0; i < fromPoints.size(); i++) {
            max = Math.max(max, calculateDistanceBetweenPointsWithPoint2D(toPoint, fromPoints.get(i)));
        }
        return max;
    }

    static public double minDistance(List<Point> fromPoints, Point toPoint) {
        double min = calculateDistanceBetweenPointsWithPoint2D(toPoint, fromPoints.get(0));
        for (int i = 1; i < fromPoints.size(); i++) {
            min = Math.min(min, calculateDistanceBetweenPointsWithPoint2D(toPoint, fromPoints.get(i)));
        }
        return min;
    }

    static public PointPair maxDistance(List<Point> points) {
        PointPair max = new PointPair(points.get(0), points.get(0));
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                if (calculateDistanceBetweenPointsWithPoint2D(points.get(i), points.get(j)) > calculateDistanceBetweenPointsWithPoint2D(max)) {
                    max = new PointPair(points.get(i), points.get(j));
                }
            }
        }
        return max;
    }

    static public Point pointAtX(Point a, Point b, int x) {
        Double slope = (b.y - a.y) / (b.x - a.x);
        int y = (int) (a.y + (x - a.x) * slope);
        return new Point(x, y);
    }

    static public BufferedImage Mat2BufferedImage(Mat matrix) throws Exception {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", matrix, mob);
        byte ba[] = mob.toArray();

        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

    static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    static Rect cangeRectForFittingMat(Mat mat, Rect rect) {
        return new Rect(Math.max(0, Math.min(rect.x, mat.width() - rect.width)),
                Math.max(0, Math.min(rect.y, mat.height() - rect.height)),
                rect.width, rect.height);
    }

    static Mat rotateMat(Mat src, double angle) {
        //Creating destination matrix
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        // Creating a Point object
        Point point = new Point(src.rows() / 2, src.cols() / 2);
        //Creating the transformation matrix
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(point, angle, 1);
        // Creating the object of the class Size
        Size size = new Size(src.cols(), src.cols());
        // Rotating the given image
        Imgproc.warpAffine(src, dst, rotationMatrix, size);

        return rotationMatrix;
    }

    public static void drawRotatedRectangle(Mat src, RotatedRect rotatedRect, Scalar color) {
        Point[] points = new Point[4];
        rotatedRect.points(points);

        // Now we can fill the rotated rectangle with our specified color
        Imgproc.fillConvexPoly(src,
                new MatOfPoint(points),
                color, 4);
    }

    public static void drawRotatedRectangle(Mat src, RotatedRect rotatedRect, Scalar color, int thickness) {
        Point[] points = new Point[4];
        rotatedRect.points(points);

        List<MatOfPoint> pointsL = new ArrayList<>();
        pointsL.add(new MatOfPoint(points));
        Imgproc.drawContours(src, pointsL, -1, color, thickness);

    }

    public static List<Pixel> getAllPixel(Mat mat) {
        return getAllPixel(mat, Mat.ones(mat.size(), mat.type()));
    }

    public static List<Pixel> getAllPixel(Mat mat, Mat mask) {
        return getAllPixel(mat, mask, null);
    }

    /**
     * Gibt nur Pixel in der gewünschten Farbe zurück
     *
     * @param mat
     * @param mask
     * @param color
     * @return
     */
    public static List<Pixel> getAllPixel(Mat mat, Mat mask, Scalar color) {
        LinkedList<Pixel> pixels = new LinkedList<>();
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                if (mask.get(i, j)[0] > 0 && (color == null || mat.get(i, j)[0] == color.val[0])) {
                    pixels.add(new Pixel(j, i, mat.get(i, j)[0]));
                }
            }
        }
        return pixels;
    }

    /**
     * Zählt Pixel im Bereich
     *
     * @param mat
     * @param mask
     * @param color
     * @return
     */
    public static int countPixel(Mat mat, Mat mask, Scalar color) {
        int x = 0;
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                if (mask.get(i, j)[0] > 0 && (color == null || mat.get(i, j)[0] == color.val[0])) {
                    x++;
                }
            }
        }
        return x;
    }

    public static void drawLineInMat(Mat mat, Point p1, double length, double angle, Scalar scalar, int thickness) {
        double x = p1.x + length * Math.cos(Math.toRadians(angle));
        double y = p1.y + length * Math.sin(Math.toRadians(angle));

        Imgproc.line(mat, p1, new Point(x, y), scalar, thickness);
    }

    public static Point pointMinusPoint(Point a, Point b) {
        return new Point(a.x - b.x, a.y - b.y);
    }

    public static Double berecheDieDurchschnittlicheVeränderungDesDatensatzes(List<Double> input) {
        Double sum = 0.0;

        for (int i = 1; i < input.size(); i++) {
            sum += input.get(i) - input.get(i - 1);
        }

        if (input.size() < 2) {
            return sum;
        } else {
            return sum / (input.size() - 1);
        }
    }

    public static Point getCenter(Rect rect) {
        return new Point(rect.x + (rect.width * 0.5), rect.y + (rect.height * 0.5));
    }

    public static Mat sharpen(Mat src) {
        Mat dest = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.GaussianBlur(src, dest, new Size(0, 0), 10);
        Core.addWeighted(src, 1.5, dest, -0.5, 0, dest);
        return dest;
    }

    public static Mat erode(Mat src,int shape,int kernelSize) {
        Mat out = src.clone();
//        Core.bitwise_not(src, out);
        Mat element = Imgproc.getStructuringElement( shape, new Size(2 * kernelSize + 1, 2 * kernelSize + 1));

        Imgproc.erode(out, out, element);
//        Core.bitwise_not(out, out);
        return out;
    }

    public static Double parseDoubleOrNAN(String string) {
        try {
            return Double.parseDouble(string);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static Mat resize(Mat mat, Size size){
        Mat out = new Mat();
        Imgproc.resize(mat,out,size);
        return out;
    }
}
