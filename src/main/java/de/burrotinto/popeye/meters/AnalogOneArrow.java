package de.burrotinto.popeye.meters;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.cv.Helper;
import lombok.SneakyThrows;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import net.sourceforge.tess4j.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class AnalogOneArrow {
    private Mat src, cannyOutput;
    private int threshold;
    private RotatedRect minEllipseDisplay;

    private Pointer pointer;

//    private double minGrad, maxGrad, minValue, maxValue;

    private final TextDedection textDedection = new TextDedection();

    private final HashMap<RotatedRect, Integer> labels = new HashMap<>();

    public AnalogOneArrow(Mat src) {
        this(src, 100, 10);
    }

    @SneakyThrows
    public AnalogOneArrow(Mat src, int threshold, int steps) {
        this.src = src.clone();
        this.threshold = threshold;
//        this.minGrad = minGrad;
//        this.maxGrad = maxGrad;
//        this.minValue = minValue;
//        this.maxValue = maxValue;

        cannyOutput = new Mat();

        Imgproc.Canny(src, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Check und initialisierung der Anzeige
        RotatedRect[] minEllipse = new RotatedRect[contours.size()];
        int indexMax = 0;
        for (int i = 0; i < contours.size(); i++) {
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5) {
                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
            }
            if (minEllipse[i].size.area() > minEllipse[indexMax].size.area()) {
                indexMax = i;
            }
        }
        minEllipseDisplay = minEllipse[indexMax];


        //Zeiger Check und initialisierung
        ArrayList<Pointer> zeigerKandidaten = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            int finalIndexMax = indexMax;
            Pointer.isPointer(contour).ifPresent(p -> {
                if (Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(finalIndexMax).toArray()), p.getDirection().p1, false) >= 0 &&
                        Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(finalIndexMax).toArray()), p.getDirection().p1, false) >= 0
                        && Math.min(Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()), Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) <= getDisplay().size.width / 4) {
                    zeigerKandidaten.add(p);
                }
            });
        }
        zeigerKandidaten.stream().max((o1, o2) -> (int) (o1.getLength() - o2.getLength())).ifPresent(p -> pointer = p);

        if (Helper.calculateDistanceBetweenPointsWithPoint2D(pointer.getDirection().p1, getCenter()) > Helper.calculateDistanceBetweenPointsWithPoint2D(pointer.getDirection().p2, getCenter())) {
            pointer.setArrow(pointer.getDirection().p1);
            pointer.setBottom(pointer.getDirection().p2);
        } else {
            pointer.setArrow(pointer.getDirection().p2);
            pointer.setBottom(pointer.getDirection().p1);
        }


//        Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(indexMax).toArray()), new Point(0, 0), false);

        Tesseract it = new Tesseract();
        it.setDatapath("data");
        it.setLanguage("digits_comma");
//        it.setTessVariable("tessedit_char_whitelist", "0123456789");


        for (RotatedRect r : textDedection.getTextAreas(src)) {
            try {
//            Rectangle rectangle = new Rectangle(r.boundingRect().x, r.boundingRect().y, r.boundingRect().width, r.boundingRect().height);
                BufferedImage sub = Helper.Mat2BufferedImage(src.submat(r.boundingRect()));
//                String s = ocr.recognize(sub,
//                        Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT); // PLAINTEXT | XML | PDF | RTF
//                System.out.println("Result: " + s);
                String str = it.doOCR(sub).replaceAll("[^0-9]", "");
//                System.out.println("tess: " + str);
//                Imgcodecs.imwrite("out-" + str + "-.png", src.submat(r.boundingRect()));
                Integer i = Integer.parseInt(str);
                if (i % steps == 0) {
                    labels.put(r, i);
                }
            } catch (Exception e) {
            }

        }
    }

    public RotatedRect getDisplay() {
        return minEllipseDisplay;
    }

    public Point getCenter() {
        return minEllipseDisplay.center;
    }

    public Pointer getPointer() {
        return pointer;
    }

    public double getWinkel() {
        return calculateWinkel(pointer.getArrow());
    }

    private double calculateWinkel(Point point) {
        double hypotenuse = Helper.calculateDistanceBetweenPointsWithPoint2D(point, getCenter());
        double ankathete = Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), new Point(point.x, getCenter().y));
//        System.out.println("hyp = " + hypotenuse);
//        System.out.println("ankathete = " + ankathete);
//        System.out.println("acos = " + Math.acos(ankathete / hypotenuse));
        double w = (180 / Math.PI) * Math.acos(ankathete / hypotenuse);
//        System.out.println("" + w);
//        System.out.println("Mitte " + getCenter());
//        System.out.println("Spitze " + getPointer().getArrow());
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

    public double getValue() {
        Iterator<Map.Entry<RotatedRect, Integer>> iterator = getLabels().entrySet().iterator();
        Map.Entry<RotatedRect, Integer> next = iterator.next();
        int min = next.getValue();
        RotatedRect minR = next.getKey();
        int max = min;
        RotatedRect maxR = minR;

        while (iterator.hasNext()) {
            next = iterator.next();
            if (min > next.getValue()) {
                min = next.getValue();
                minR = next.getKey();
            } else if (max < next.getValue()) {
                max = next.getValue();
                maxR = next.getKey();
            }
        }

        double minW = calculateWinkel(minR.center);
        double maxW = calculateWinkel(maxR.center);


        double xPP = Math.abs(max - min) / Math.abs((minW - maxW + 360) % 360);
        double pointer = getWinkel();
        double x = (minW - pointer);

        return min + (x * xPP);

//        double range = (minGrad - maxGrad + 360) % 360;
//        double p = (minGrad - getWinkel() + 360) % 360;
//        System.out.println();
//        return ((p / range) * (maxValue - minValue)) + minValue;

    }

    public Map<RotatedRect, Integer> getLabels() {
        return labels;
    }
}
