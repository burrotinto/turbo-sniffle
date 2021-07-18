package de.burrotinto.turboSniffle.meters.gauge.test;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.PointPair;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;
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
    @Getter
    final private List<MatOfPoint> contour ;
    @Getter
    final private List<MatOfPoint> hullList = new ArrayList<>();
    final private RotatedRect minRect;

    @Getter
    @Setter
    private Point arrow;
    @Getter
    @Setter
    private Point bottom;

    public Pointer(MatOfPoint contour) {
        ArrayList<MatOfPoint> l = new ArrayList<>();
        this.contour = l;
        this.contour.add(contour);
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

        arrow = getDirection().p2;
        bottom = getDirection().p1;
    }


    public double getLength() {
        return Helper.calculateDistanceBetweenPointsWithPoint2D(getDirection());
    }

    public double getWidth() {
        return contour.get(0).size().width;
    }

    public double scale() {
        return getLength() / getWidth();
    }

    public PointPair getDirection() {
        return Helper.maxDistance(contour.get(0).toList());
    }

    public RotatedRect getMinRect() {
        return minRect;
    }

    static public Optional<Pointer> isPointer(MatOfPoint contour, Mat gaugeMat, RotatedRect gauge) {
        Pointer p = new Pointer(contour);
//        if (p.scale() > 10 && p.scale() < 100) {
        return Optional.of(p);
//        } else
//            return Optional.empty();
    }
}
