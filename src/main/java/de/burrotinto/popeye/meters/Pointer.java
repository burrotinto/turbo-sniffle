package de.burrotinto.popeye.meters;

import de.burrotinto.popeye.transformation.Helper;
import de.burrotinto.popeye.transformation.PointPair;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Pointer {

    final private MatOfPoint contour;
    final private List<MatOfPoint> hullList = new ArrayList<>();
    final private RotatedRect minRect;

    public Pointer(MatOfPoint contour) {
        this.contour = contour;
        //Minimales Rechteck
        minRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
//        minRect = Imgproc.boundingRect(new MatOfPoint2f(contour.toArray()));

        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        Point[] contourArray = contour.toArray();
        Point[] hullPoints = new Point[hull.rows()];
        List<Integer> hullContourIdxList = hull.toList();
        for (int i = 0; i < hullContourIdxList.size(); i++) {
            hullPoints[i] = contourArray[hullContourIdxList.get(i)];
        }
        hullList.add(new MatOfPoint(hullPoints));

    }


    public double getLength() {
        return Helper.calculateDistanceBetweenPointsWithPoint2D(getDirection());
    }

    public double getWidth() {
        return contour.size().width;
    }

    public double scale() {
        return getLength() / getWidth();
    }

    public PointPair getDirection() {
        return Helper.maxDistance(contour.toList());
    }

    public RotatedRect getMinRect() {
        return minRect;
    }

    static public Optional<Pointer> isPointer(MatOfPoint contour) {
        Pointer p = new Pointer(contour);
        if (p.scale() > 10 && p.scale() < 100) {

            return Optional.of(p);
        } else
            return Optional.empty();
    }
}
