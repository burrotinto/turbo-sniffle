package de.burrotinto.turboSniffle.cv;

import de.burrotinto.turboSniffle.gauge.Gauge;
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
    private static final Scalar scalar10 = new Scalar(10, 10, 10);

    @Getter
    private Mat canny;

    @Getter
    private Map<RotatedRect, Mat> singleHeads;

    @Getter
    private Point center;

    private Mat heatmap;

    /**
     * Zeichnet ein Kreuz in jede gefundene Kontur
     *
     * @return
     */
    public HeatMap(Mat canny) {
        this.canny = canny;

        Map<RotatedRect, Mat> map = new HashMap<>();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(getCanny(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

//        Mat draw = new Mat(canny.size(), CvType.CV_8UC3);

        for (int i = 0; i < contours.size(); i++) {
            val rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));

            //Nur Konturen die eine Mindestgröße besitzen
            if (rect.size.area() > 32 &&  Math.min(rect.size.height, rect.size.width) >= 3) {
                Mat xxx = Mat.zeros(canny.size(), Gauge.TYPE);

                double angle = rect.angle;
                if (rect.size.width < rect.size.height) {
                    angle = 90 + angle;
                }

                int s = (int) Math.max(5, Math.min(rect.size.height, rect.size.width));


                Helper.drawLineInMat(xxx, rect.center, canny.size().height, angle, scalar1, s);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (angle + 180) % 360, scalar1, s);

                Helper.drawLineInMat(xxx, rect.center, canny.size().height, angle, scalar10, 1);
                Helper.drawLineInMat(xxx, rect.center, canny.size().height, (angle + 180) % 360, scalar10, 1);
                map.put(rect, xxx);

//                //Malen
//                Helper.drawLineInMat(draw, rect.center, canny.size().height, angle, new Scalar(0,25,0), s);
//                Helper.drawLineInMat(draw, rect.center, canny.size().height, (angle + 180) % 360, new Scalar(0,25,0), s);
//                Helper.drawLineInMat(draw, rect.center, canny.size().height, angle, new Scalar(0,250,0), 1);
//                Helper.drawLineInMat(draw, rect.center, canny.size().height, (angle + 180) % 360, new Scalar(0,250,0), 1);
//                Imgproc.drawContours(draw, contours, i, new Scalar(0, 0, 255),3);
//                Helper.drawRotatedRectangle(draw,rect,new Scalar(255,0,0),3);
            }
        }
        singleHeads = map;
//        HighGui.imshow("", draw);
//        HighGui.waitKey();
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
        return getSingleHeads().entrySet().stream().filter(rotatedRectMatEntry -> rotatedRectMatEntry.getValue().get((int) center.y, (int) center.x)[0] > 0 && Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRectMatEntry.getKey().center,getCenter()) >= Math.sqrt(Gauge.DEFAULT_SIZE.area())/3).map(rotatedRectMatEntry -> rotatedRectMatEntry.getKey()).collect(Collectors.toList());
    }

    public List<RotatedRect> getAllRect() {
        return new ArrayList<>(singleHeads.keySet());
    }
}
