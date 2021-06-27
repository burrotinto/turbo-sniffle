package de.burrotinto.turboSniffle.meters.gauge.impl;

import de.burrotinto.turboSniffle.booleanAutoEncoder.BooleanAutoencoder;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.GaugeExtraction;
import de.burrotinto.turboSniffle.meters.gauge.GaugeOnePointerLearningDataset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScaleMarkExtraction {

    public static void extract(Gauge gauge) {
        val canny = gauge.getCanny();

        Imgproc.circle(canny, gauge.getCenter(), (int) gauge.getRadius() / 2, new Scalar(0, 0, 0), -1);
        Imgproc.circle(canny, gauge.getCenter(), (int) gauge.getRadius(), new Scalar(0, 0, 0), 50);

        val g = gauge.getSource().clone();
        Imgproc.resize(g, g, new Size(512, 512));

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<RechteckCluster> rotatedRects = contours.stream()
                .map(matOfPoint -> new RechteckCluster(Imgproc.minAreaRect(
                        new MatOfPoint2f(matOfPoint.toArray()))
                        , gauge)).collect(Collectors.toList());

        DBSCANClusterer<RechteckCluster> dbscanClusterer = new DBSCANClusterer<>(50, 5);
        List<Cluster<RechteckCluster>> cluster = dbscanClusterer.cluster(rotatedRects);
        HighGui.imshow("canny", canny);


        for (int i = 0; i < cluster.size(); i++) {
            Mat draw = Mat.zeros(canny.size(), canny.type());
            List<MatOfPoint> l = new ArrayList<>();
            cluster.get(i).getPoints().forEach(rechteckCluster -> {
                Point[] p = new Point[4];
                rechteckCluster.rotatedRect.points(p);
                MatOfPoint m = new MatOfPoint(p);
                if (rechteckCluster.rotatedRect.size.area() > 25) {
                    l.add(m);
                }

            });

            Imgproc.drawContours(draw, l, -1, new Scalar(255, 255, 255), -1);
            Imgproc.drawContours(g, l, -1, new Scalar(255, 255, 255), -1);
            Imgproc.drawContours(g, l, -1, new Scalar(255, 255, 255), 3);
            HighGui.imshow("" + i, draw);
        }

        val draw = Mat.zeros(canny.size(), canny.type());
        Imgproc.drawContours(draw, contours, -1, new Scalar(255, 255, 255), 1);


        TextDedection td = new TextDedection();
        List<MatOfPoint> l = new ArrayList<>();
        td.getTextAreas(gauge.getSource()).forEach(rotatedRect -> {
            Point[] p = new Point[4];
            rotatedRect.points(p);
            MatOfPoint m = new MatOfPoint(p);
            l.add(m);
        });
        Imgproc.drawContours(g,l,-1,new Scalar(255,255,255),-1);
        autoencoder(g);


        HighGui.imshow("final", g);
        HighGui.waitKey();
        System.out.println("");
        return;
    }


    private static void autoencoder(Mat gauge){
        val train = GaugeOnePointerLearningDataset.getTrainingset(Gauge.DEFAULT_SIZE, 1);

        long min = Long.MAX_VALUE;
        int iMin = 0;
        for (int i = 0; i < train.size(); i++) {
            val dist = BooleanAutoencoder.DISTANZ(train.get(i).getSource(), gauge, 85,min);
            if (dist < min) {
                min = dist;
                iMin = i;
            }
        }

        HighGui.imshow("AUTOENCODDER 123",gauge);
        HighGui.imshow("AUTOENCODDER", train.get(iMin).getSource());
    }

    @Getter
    @AllArgsConstructor
    private static class RechteckCluster implements Clusterable {
        public RotatedRect rotatedRect;
        public Gauge gauge;


        @Override
        public double[] getPoint() {
            val d = new double[5];
            d[0] = Helper.calculateDistanceBetweenPointsWithPoint2D(gauge.getCenter(), rotatedRect.center)*2;
            d[1] = rotatedRect.size.area();
            d[2] = rotatedRect.angle;
            d[3] = (rotatedRect.size.height / rotatedRect.size.width);
            d[4] = gauge.getSource().get((int)rotatedRect.center.x,(int)rotatedRect.center.y)[0];
            return d;
        }
    }

    @Getter
    @AllArgsConstructor
    private static class Pixel implements Clusterable {
        public Point point;
        public int color;

        @Override
        public double[] getPoint() {
            val d = new double[3];
            d[0] = color;
            d[1] = point.x;
            d[2] = point.y;
            return d;
        }
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        extract(GaugeExtraction.extract(Imgcodecs.imread("data/example/temp.jpg")));
    }
}

