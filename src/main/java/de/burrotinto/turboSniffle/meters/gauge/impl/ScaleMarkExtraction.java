package de.burrotinto.turboSniffle.meters.gauge.impl;


import de.burrotinto.turboSniffle.cv.Helper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScaleMarkExtraction {

    public static Mat autoExtract(Mat canny, Mat greyMat) {
        int esp = 1;
        return null;
    }

    public static List<RotatedRect> extract(Mat canny, Mat greyMat, int esp) {

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        List<RechteckCluster> rotatedRects = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {

            val rect = Imgproc.minAreaRect(
                    new MatOfPoint2f(contours.get(i).toArray()));

            if (rect.size.area() > 25) {
                Mat mask = Mat.zeros(greyMat.size(), CvType.CV_8U);
                List<MatOfPoint> l = new ArrayList<>();
                l.add(contours.get(i));
                Imgproc.drawContours(mask, l, 0, new Scalar(255, 255, 255), -1);

                double color = Core.mean(greyMat, mask).val[0];


                rotatedRects.add(new RechteckCluster(rect
                        , new Point(canny.width() / 2, canny.rows() / 2), color, contours.get(i).toList()));
            }
        }

        DBSCANClusterer<RechteckCluster> dbscanClusterer = new DBSCANClusterer<>(esp, 10);
        List<Cluster<RechteckCluster>> cluster = dbscanClusterer.cluster(rotatedRects);


        Mat out = Mat.zeros(canny.size(), canny.type());
        int maxPoint = 0;

        for (int i = 0; i < cluster.size(); i++) {

            List<MatOfPoint> l = new ArrayList<>();
            cluster.get(i).getPoints().forEach(rechteckCluster -> {
                Point[] p = new Point[4];
                rechteckCluster.rotatedRect.points(p);
                MatOfPoint m = new MatOfPoint(p);
                l.add(m);
            });

            if (cluster.get(maxPoint).getPoints().size() <= cluster.get(i).getPoints().size()) {
                maxPoint = i;
                out = Mat.zeros(canny.size(), canny.type());
                for (int j = 0; j < cluster.get(i).getPoints().size(); j++) {
                    Helper.drawRotatedRectangle(out, cluster.get(i).getPoints().get(j).getRotatedRect(), Helper.WHITE);
                }
                Imgproc.drawContours(out, l, -1, new Scalar(255, 255, 255), -1);
            }
        }

        return cluster.get(maxPoint).getPoints().stream().map(rechteckCluster -> rechteckCluster.rotatedRect).collect(Collectors.toList());
//        ArrayList<Point> points = new ArrayList<>();
//        cluster.get(maxPoint).getPoints().stream().forEach(rechteckCluster -> points.addAll(rechteckCluster.contour));
//
//        return points;
    }


    @Getter
    @AllArgsConstructor
    private static class RechteckCluster implements Clusterable {
        public RotatedRect rotatedRect;
        public Point center;
        public double color;
        public List<Point> contour;

        @Override
        public double[] getPoint() {
            val d = new double[2];
//            d[0] = Helper.calculateDistanceBetweenPointsWithPoint2D(center, rotatedRect.center);
            d[0] = Helper.maxDistance(contour, center);
            d[1] = Math.min(rotatedRect.size.height, rotatedRect.size.width);
//            d[3] = rotatedRect.center.y;
//            d[4] = rotatedRect.center.x;
//            d[1] = rotatedRect.size.width * rotatedRect.size.height;

//            d[2] = (Math.max(rotatedRect.size.height, rotatedRect.size.width) / Math.min(rotatedRect.size.height, rotatedRect.size.width));
//            d[4] = gauge.getSource().get((int)rotatedRect.center.x,(int)rotatedRect.center.y)[0];
            return d;
        }
    }

//    @Getter
//    @AllArgsConstructor
//    private static class Pixel implements Clusterable {
//        public Point point;
//        public int color;
//
//        @Override
//        public double[] getPoint() {
//            val d = new double[3];
//            d[0] = color;
//            d[1] = point.x;
//            d[2] = point.y;
//            return d;
//        }
//    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

//        extract(GaugeExtraction.extract(Imgcodecs.imread("data/example/testTemp.jpg"), "").getAusgerolltCanny());
    }
}

