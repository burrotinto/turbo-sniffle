package de.burrotinto.turboSniffle.meters.gauge.test;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.Getter;
import lombok.val;
import org.nd4j.linalg.primitives.AtomicDouble;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HeatMap {
    private static final Scalar scalar1 = new Scalar(1, 1, 1);
    private static final Scalar scalar5 = new Scalar(5, 5, 5);

    @Getter
    private Mat canny;

    @Getter
    private Map<RotatedRect, Mat> singleHeads;

    @Getter
    private Point center;

    private Mat heatmap;

    /**
     * Zeichnet ein Kreuz in jede gefundene Contour
     *
     * @return
     */
    public HeatMap(Mat canny) {
        this.canny = canny;

        Map<RotatedRect, Mat> map = new HashMap<>();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(getCanny(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        for (int i = 0; i < contours.size(); i++) {
            val rect = Imgproc.minAreaRect(
                    new MatOfPoint2f(contours.get(i).toArray()));
            if (rect.size.area() > 50) {
                Mat xxx = Mat.zeros(canny.size(), Gauge.TYPE);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, rect.angle, scalar1, 5);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (rect.angle + 90) % 360, scalar1,  5);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (rect.angle + 180) % 360, scalar1,5);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (rect.angle + 270) % 360, scalar1,5);

                Helper.drawLineInMat(xxx, rect.center, canny.size().height, rect.angle, scalar5, 1);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (rect.angle + 90) % 360, scalar5,  1);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (rect.angle + 180) % 360, scalar5,1);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (rect.angle + 270) % 360, scalar5,1);
                map.put(rect, xxx);
            }
        }
        singleHeads = map;

        calcCenter();
    }

    public Mat getHeadMat() {
        if (heatmap == null) {
            Mat draw = Mat.zeros(canny.size(), canny.type());
            getSingleHeads().forEach((rotatedRect, mat) -> Core.add(draw, mat, draw));
            heatmap = draw;
        }
        return heatmap;
    }

    public Mat getHeadMatSkaliert() {
        Mat draw = heatmap;

        double max = draw.get((int) center.y, (int) center.x)[0];
        double scale = 255 / max;

        Core.multiply(draw, new Scalar(scale), draw);

        return draw;
    }

    private void calcCenter() {

        //Die Hellsten Pixel Finden
        List<Pixel> maxPixels = new ArrayList<>();
        List<Pixel> allPixels = Helper.getAllPixel(getHeadMat()).stream().filter(pixel -> pixel.color > 0).collect(Collectors.toList());
        for (int i = 0; i < allPixels.size(); i++) {
            if (maxPixels.isEmpty()) {
                maxPixels = new ArrayList<>();
                maxPixels.add(allPixels.get(i));
            } else if (maxPixels.get(0).color == allPixels.get(i).color) {
                maxPixels.add(allPixels.get(i));
            } else if (maxPixels.get(0).color < allPixels.get(i).color) {
                maxPixels = new ArrayList<>();
                maxPixels.add(allPixels.get(i));
            }

        }

        //Durchschnitt der Hellsten Pixel
        AtomicDouble x = new AtomicDouble(0);
        AtomicDouble y = new AtomicDouble(0);

        maxPixels.forEach(pixel -> {
            x.addAndGet(pixel.point.x);
            y.addAndGet(pixel.point.y);
        });

        center = new Point(x.get() / maxPixels.size(), y.get() / maxPixels.size());
    }


    public List<RotatedRect> getAllConnectedWithCenter() {
        Point center = getCenter();
        return getSingleHeads().entrySet().stream().filter(rotatedRectMatEntry -> rotatedRectMatEntry.getValue().get((int) center.y, (int) center.x)[0] > 0).map(rotatedRectMatEntry -> rotatedRectMatEntry.getKey()).collect(Collectors.toList());
    }

}
