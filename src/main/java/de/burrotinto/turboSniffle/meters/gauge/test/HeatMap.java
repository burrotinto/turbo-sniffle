package de.burrotinto.turboSniffle.meters.gauge.test;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.nd4j.linalg.primitives.AtomicDouble;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class HeatMap {

    @Getter
    private Mat canny;

    @Getter
    private Map<RotatedRect, Mat> singleHeads;

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
                Mat xxx = Mat.zeros(Gauge.DEFAULT_SIZE, Gauge.TYPE);
                Helper.drawLineInMat(xxx, rect.center, Gauge.DEFAULT_SIZE.height, rect.angle, new Scalar(1, 1, 1), (int) rect.size.width);
                Helper.drawLineInMat(xxx, rect.center, Gauge.DEFAULT_SIZE.height, (rect.angle + 90) % 360, new Scalar(1, 1, 1), (int) rect.size.height);
                Helper.drawLineInMat(xxx, rect.center, Gauge.DEFAULT_SIZE.height, (rect.angle + 180) % 360, new Scalar(1, 1, 1), (int) rect.size.width);
                Helper.drawLineInMat(xxx, rect.center, Gauge.DEFAULT_SIZE.height, (rect.angle + 270) % 360, new Scalar(1, 1, 1), (int) rect.size.height);
                map.put(rect, xxx);
            }
        }
        singleHeads = map;
    }

    public Mat getHeadMat() {
        Mat draw = Mat.zeros(canny.size(), canny.type());
        getSingleHeads().forEach((rotatedRect, mat) -> Core.add(draw, mat, draw));
        return draw;
    }

    public Point getCenter() {
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
        AtomicDouble x = new AtomicDouble(0);
        AtomicDouble y = new AtomicDouble(0);
        maxPixels.forEach(pixel -> {
            x.addAndGet(pixel.point.x);
            y.addAndGet(pixel.point.y);
        });

        return new Point(x.get() / maxPixels.size(), y.get() / maxPixels.size());
    }

    public List<RotatedRect> getAllConnectedWithCenter() {
        Point center = getCenter();
        return getSingleHeads().entrySet().stream().filter(rotatedRectMatEntry -> rotatedRectMatEntry.getValue().get((int) center.y, (int) center.x)[0] > 0).map(rotatedRectMatEntry -> rotatedRectMatEntry.getKey()).collect(Collectors.toList());
    }
}
