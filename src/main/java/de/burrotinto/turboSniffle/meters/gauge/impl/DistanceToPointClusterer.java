package de.burrotinto.turboSniffle.meters.gauge.impl;


import de.burrotinto.turboSniffle.cv.Helper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DistanceToPointClusterer {

    public static List<RotatedRect> extract(List<RotatedRect> rotatedRects, Point center, int esp, int minPts) {
        if (rotatedRects.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<DistanceCluster> distance = new ArrayList<>();

        rotatedRects.forEach(rotatedRect -> distance.add(new DistanceCluster(rotatedRect, center)));

        DBSCANClusterer<DistanceCluster> dbscanClusterer = new DBSCANClusterer<>(esp, minPts);
        List<Cluster<DistanceCluster>> cluster = dbscanClusterer.cluster(distance);

        cluster.sort((o1, o2) -> o2.getPoints().size() - o1.getPoints().size());

        if (cluster.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            return cluster.get(0).getPoints().stream().map(distanceCluster -> distanceCluster.rotatedRect).collect(Collectors.toList());
        }
    }


    @Getter
    @AllArgsConstructor
    private static class DistanceCluster implements Clusterable {
        public RotatedRect rotatedRect;
        public Point center;

        @Override
        public double[] getPoint() {
            val d = new double[2];
            d[0] = Helper.calculateDistanceBetweenPointsWithPoint2D(center, rotatedRect.center);
            d[1] = rotatedRect.size.area();
            return d;
        }
    }


}

