package de.burrotinto.popeye.transformation;

import lombok.AllArgsConstructor;
import org.opencv.core.Point;

import java.awt.geom.Point2D;
import java.util.List;

public class Helper {
    static public double calculateDistanceBetweenPointsWithPoint2D(Point x, Point y) {
        return Point2D.distance(x.x, x.y, y.x, y.y);
    }

    static public double calculateDistanceBetweenPointsWithPoint2D(Pair<Point> pair) {
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

    static public  Point pointAtX(Point a, Point b, int x) {
        Double slope = (b.y - a.y) / (b.x - a.x);
        int y = (int) (a.y + (x - a.x) * slope);
        return new Point(x,y);
    }
}
