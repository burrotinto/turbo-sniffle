package de.burrotinto.turboSniffle.meters.gauge.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Clustering {

    static public List<MatOfPoint> getObjects(Mat src, int maxGreyscale) {
        DBSCANClusterer<Pixel> dbscanClusterer = new DBSCANClusterer<>(3, (src.cols() * src.height()) / 1000);
        LinkedList<Pixel> pixels = new LinkedList<>();
        for (int i = 0; i <src.cols() * src.height(); i++) {
            double[] p = new double[2];
            p[1] = i % src.width();
            p[0] = i / src.height();
            int c = (int) src.get((int) p[1], (int) p[0])[0];
            if (c <= maxGreyscale) {
                pixels.add(new Pixel(new Point(p), c));
            }
        }
        List<Cluster<Pixel>> cluster = dbscanClusterer.cluster(pixels);
        return cluster.stream().map(pixelCluster -> {
            MatOfPoint mp = new MatOfPoint();
            mp.fromList(pixelCluster.getPoints().stream().map(pixel -> pixel.point).collect(Collectors.toList()));
            return mp;
        }).collect(Collectors.toList());
    }


}


@Getter
@AllArgsConstructor
class Pixel implements Clusterable {
    public Point point;
    public int color;

    @Override
    public double[] getPoint() {
        val d = new double[3];
        d[0] = color/10;
        d[1] = point.x;
        d[2] = point.y;
        return d;
    }
}