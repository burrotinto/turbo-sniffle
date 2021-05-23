package de.burrotinto.turboSniffle.cv;

import de.burrotinto.popeye.transformation.Pair;
import de.burrotinto.popeye.transformation.PointPair;
import lombok.AllArgsConstructor;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

public class Helper {
    static public double calculateDistanceBetweenPointsWithPoint2D(Point x, Point y) {
        return Point2D.distance(x.x, x.y, y.x, y.y);
    }

    static public double calculateDistanceBetweenPointsWithPoint2D(Pair<Point, Point> pair) {
        return calculateDistanceBetweenPointsWithPoint2D(pair.p1, pair.p2);
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
        Imgcodecs.imencode(".jpg", matrix, mob);
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
        return new Rect(Math.max(0, Math.min(rect.x, mat.width()-rect.width)),
                Math.max(0, Math.min(rect.y, mat.height()-rect.height)),
                rect.width, rect.height);
    }

    static Mat rotateMat(Mat src, double angle){
        //Creating destination matrix
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        // Creating a Point object
        Point point = new Point(src.rows()/2,  src.cols()/2);
        //Creating the transformation matrix
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(point, angle, 1);
        // Creating the object of the class Size
        Size size = new Size(src.cols(), src.cols());
        // Rotating the given image
        Imgproc.warpAffine(src, dst, rotationMatrix, size);

        return rotationMatrix;
    }
}
