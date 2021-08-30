package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.impl.HeatMap;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.stream.Collectors;


public class Gauge {
    public final static int TYPE = CvType.CV_8U;
    public final static Size DEFAULT_SIZE = new Size(256, 256);

    @Getter
    protected Mat source;
    @Getter
    protected Mat canny;
    @Getter
    protected Mat otsu = new Mat();

    @Getter
    @Setter
    private HeatMap heatMap;

    Gauge(Mat source, Mat canny, Mat otsu) {
        //Convertiere in Grau
        if (source.type() == TYPE) {
            this.source = source;
        } else {
            this.source = Mat.zeros(source.size(), TYPE);
            Imgproc.cvtColor(source, this.source, Imgproc.COLOR_BGR2GRAY);
        }
        this.canny = canny;

        Imgproc.resize(this.source, this.source, DEFAULT_SIZE);
        if (canny != null) {
            Imgproc.resize(this.canny, this.canny, DEFAULT_SIZE);
        }

        // Otsu Binary
        if (otsu == null) {
            Imgproc.threshold(source, this.otsu, 0, 255, Imgproc.THRESH_OTSU);
        } else {
            Imgproc.resize(otsu, this.otsu, DEFAULT_SIZE);
        }
    }


    public Point getCenter() {
        return new Point(source.size().width / 2, source.size().height / 2);
    }

    public double getRadius() {
        return source.size().width / 2;
    }

    protected double bildkoordinatenZuPoolar(Point point) {
        double w = Math.toDegrees(Math.atan2(point.y - getCenter().y, point.x - getCenter().x));
        return Math.abs(360 - w) % 360;
    }

    protected Point poolarZuBildkoordinaten(double angel, double r) {
        double x = r * Math.cos(Math.toRadians(angel));
        double y = r * Math.sin(Math.toRadians(angel));
        return new Point(x + getCenter().x, getCenter().y - y);
    }

    private List<Pixel> getPixelsForAngle(List<Pixel> pixels, double angle, int nk) {
        return pixels.stream().filter(pixel -> Math.abs(Precision.round(bildkoordinatenZuPoolar(pixel.point), nk) % 360 - Precision.round(angle, nk) % 360) < Math.pow(10, -nk)).collect(Collectors.toList());
    }
}
