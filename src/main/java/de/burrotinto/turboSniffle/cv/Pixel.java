package de.burrotinto.turboSniffle.cv;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.opencv.core.Point;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class Pixel implements Clusterable, Comparable<Pixel> {
    public Point point;
    public int color;

    public Pixel(double x, double y, double color) {
        this(new Point(x, y), (int) color);
    }

    @Override
    public double[] getPoint() {
        val d = new double[3];
        d[0] = color / 10;
        d[1] = point.x;
        d[2] = point.y;
        return d;
    }

    @Override
    public int compareTo(Pixel o) {
        return color - o.color;
    }


}
