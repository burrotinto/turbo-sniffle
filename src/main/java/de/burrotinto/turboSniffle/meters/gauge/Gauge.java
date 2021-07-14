package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.Getter;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class Gauge {
    public final static int TYPE = CvType.CV_8U;
    public final static Size DEFAULT_SIZE = new Size(512, 512);
    public final static int AUFROLL_STEPS = 720;

    @Getter
    protected Mat source;
    @Getter
    protected Mat canny;
    @Getter
    protected Mat otsu = new Mat();

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

        //Wenn Wenn es mehr Schwarz als weiß gibt müssen faren getauscht werden
        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);

        List<Pixel> pixels = Helper.getAllPixel(this.otsu, mask);
        if (pixels.stream().filter(pixel -> pixel.color == 0).count() > pixels.size() / 2) {
            Core.bitwise_not(this.otsu, this.otsu);
        }


    }

    public Mat toSize(Size size) {
        Mat out = new Mat();
        Imgproc.resize(source, out, size);
        return out;
    }

    public Point getCenter() {
        return new Point(source.size().width / 2, source.size().height / 2);
    }

    public double getRadius() {
        return source.size().width / 2;
    }


    protected double calculateWinkel(Point point) {
        double hypotenuse = Helper.calculateDistanceBetweenPointsWithPoint2D(point, getCenter());
        double ankathete = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), new Point(point.x, getCenter().y));
        double w = (180 / Math.PI) * Math.acos(ankathete / hypotenuse);
        if (point.x < getCenter().x && point.y < getCenter().y) {
            return 180 - w;
        } else if (point.x < getCenter().x && point.y > getCenter().y) {
            return 180 + w;
        } else if (point.x > getCenter().x && point.y > getCenter().y) {
            return 360 - w;
        } else {
            return w;
        }
    }


    /**
     * Chi et al.
     * Machine Vision Based Automatic Detection Method of
     * Indicating Values of a Pointer Gauge
     * <p>
     * Step 4
     */
    public Mat getScaleMarks() {
        Mat otsu = new Mat();
        Imgproc.threshold(source, otsu, 0, 255, Imgproc.THRESH_OTSU);

        Mat mask = Mat.zeros(DEFAULT_SIZE, TYPE);
        Imgproc.circle(mask, getCenter(), (int) getRadius(), Helper.WHITE, -1);

        List<Pixel> pixels = Helper.getAllPixel(otsu, mask).stream().filter(pixel -> pixel.color == 0).collect(Collectors.toList());

        Mat draw = Mat.zeros(DEFAULT_SIZE, TYPE);

        pixels.forEach(pixel -> Imgproc.line(draw, pixel.point, getCenter(), Helper.WHITE));

        HashMap<Double, ArrayList<Pixel>> map = new HashMap<>();
        pixels.forEach(pixel -> {
            double angle = ((int) (calculateWinkel(pixel.point) * 10)) * 0.1;
            map.putIfAbsent(angle, new ArrayList<>());
            map.get(angle).add(pixel);
        });

        return draw;
    }


    public Point poolarZuKartesisch(double winkel, double r) {
        double x = r * Math.cos(Math.toRadians(winkel));
        double y = r * Math.sin(Math.toRadians(winkel));
        System.out.println("x=" + x + "; y=" + y);
        return new Point(x + getCenter().x, getCenter().y - y);

    }


}
