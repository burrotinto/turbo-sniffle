package de.burrotinto.turboSniffle.meters.gauge.test;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.popeye.transformation.Pair;
import de.burrotinto.turboSniffle.meters.gauge.Measuring;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class CirceGaugeOnePointer extends CircleGauge implements Measuring {
    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV = 1;

    private final TextDedection textDedection;

    private final HashMap<RotatedRect, Integer> labels = new HashMap<>();
    private final Optional<Integer> steps;

    private Mat cannyOutput;
    private int threshold;
    private RotatedRect minEllipseDisplay;
    private int indexDisplay = 0;

    private Pointer[] pointer = new Pointer[0];


    public CirceGaugeOnePointer(Mat src, TextDedection textDedection) {
        this(src, 100, Optional.of(10),textDedection);
    }

    @SneakyThrows
    public CirceGaugeOnePointer(Mat inputSrc, int threshold, Optional<Integer> steps, TextDedection textDedection) {
        super(threshold);
        this.textDedection = textDedection;
        this.threshold = threshold;
        this.steps = steps;

        update(inputSrc);
    }


    public Pointer[] getPointer() {
        if (pointer.length == 0) {

            ArrayList<Pointer> zeigerKandidaten = new ArrayList<>();

            for (MatOfPoint contour : contours) {
                Pointer p = new Pointer(contour);

                //Richtung des Zeigers ermitteln
                if (Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p1, getCenter()) > Helper.calculateDistanceBetweenPointsWithPoint2D(p.getDirection().p2, getCenter())) {
                    p.setArrow(p.getDirection().p1);
                    p.setBottom(p.getDirection().p2);
                } else {
                    p.setArrow(p.getDirection().p2);
                    p.setBottom(p.getDirection().p1);
                }

                if (Helper.calculateDistanceBetweenPointsWithPoint2D(getGauge().center, p.getArrow()) < getDurchmesser() / 2
                        && Helper.calculateDistanceBetweenPointsWithPoint2D(getGauge().center, p.getBottom()) < getDurchmesser() / 3
                ) {
                    zeigerKandidaten.add(p);
                }
            }

            //Längsten Kandidaten auswählen
            zeigerKandidaten.stream().max((o1, o2) -> (int) (o1.getLength() - o2.getLength())).ifPresent(pointer1 -> {
                pointer = new Pointer[1];
                pointer[0] = pointer1;
            });



        }
        return pointer;
    }

    public double getWinkel() {
        // Winkel berechnung nach
        // A pointer location algorithm for computer visionbased automatic reading recognition of pointer gauges
        // https://www.degruyter.com/document/doi/10.1515/phys-2019-0010/html
        return calculateWinkel(getPointer()[0].getArrow());
    }

    public Mat getDrawing() {
        Mat drawing = getGaugeMat().clone();
        val rng = new Random();

        Scalar color = getColor();

        // rotated rectangle
        Imgproc.drawMarker(drawing, getCenter(), color);

        if (getPointer().length > 0) {
            Imgproc.line(drawing, getPointer()[0].getArrow(), getCenter(), color, 5);
            Imgproc.circle(drawing, getCenter(), (int) Helper.calculateDistanceBetweenPointsWithPoint2D(getCenter(), getPointer()[0].getArrow()), color);
//            Imgproc.drawMarker(drawing, getPointer()[0].getArrow(), color);
//            Imgproc.drawMarker(drawing, getPointer()[0].getMinRect().center, color);
        }

//        Imgproc.putText(drawing, getWinkel() + "°", getCenter(), 0, 1,new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)));

        Imgproc.putText(drawing, (int) getValue() + " ", getCenter(), 0, 1, getColor());
        getLabels().entrySet().stream().forEach(entry -> {
            Imgproc.putText(drawing, entry.getValue().toString(), entry.getKey().center, 0, 0.5, getColor());
        });
        return drawing;
    }

    private double calculateWinkel(Point point) {
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

    public double getValue() {
        double pointer = getWinkel();

        LinkedList<Pair<Double, Map.Entry<RotatedRect, Integer>>> pairs = new LinkedList<>();
        getLabels().entrySet().stream().forEach(e -> {
            Double x = Math.abs(calculateWinkel(e.getKey().center) - pointer);
            pairs.add(new Pair<>(x, e));
            pairs.add(new Pair<>(360 - x, e));
        });

        //Sortierung nach entfernung zum Zeiger
        pairs.sort((o1, o2) -> (int) (o1.p1 - o2.p1));

        double minW = calculateWinkel(pairs.get(0).p2.getKey().center);
        double maxW = calculateWinkel(pairs.get(1).p2.getKey().center);


        //Bestimmen eines Wertes pro Prozent
        double xPP =  Math.abs(pairs.get(0).p2.getValue() - pairs.get(1).p2.getValue())/(Math.abs((minW - maxW) % 360));
        //Delta zum Zähler
        double x = (minW - pointer);

        return pairs.get(0).p2.getValue() + (x * xPP);
    }

    @Override
    public void update(Mat nmat) {
        //initialisation
        super.update(nmat);

        pointer = new Pointer[0];
        indexDisplay = 0;


        //Canny and Contours finding
        cannyOutput = getEdgeDedectionCanny(getRefittetSrcMat());

        Imgproc.GaussianBlur(cannyOutput, cannyOutput, new Size(5, 5), 0);
//        Imgproc.Sobel(cannyOutput, cannyOutput, -1, 1, 0);
//        Imgproc.threshold(cannyOutput, cannyOutput, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);
//        Imgproc.adaptiveThreshold(cannyOutput, cannyOutput, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 75, 35);
//        Imgproc.adaptiveThreshold(cannyOutput, cannyOutput, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 99, 4);


        contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        //Check und initialisierung der Anzeige
        RotatedRect[] minEllipse = new RotatedRect[contours.size()];

        for (int i = 0; i < contours.size(); i++) {
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5) {
                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
            }
            if (minEllipse[i].size.area() > minEllipse[indexDisplay].size.area()) {
                indexDisplay = i;
            }
        }
        minEllipseDisplay = minEllipse[indexDisplay];


        // Beschriftung erkennung
        val textAreas = textDedection.getTextAreas(getRefittetSrcMat());
        val maxDist = Helper.calculateDistanceBetweenPointsWithPoint2D(textAreas.stream().max((o1, o2) -> (int) (Helper.calculateDistanceBetweenPointsWithPoint2D(o1.center, getCenter()) - Helper.calculateDistanceBetweenPointsWithPoint2D(o2.center, getCenter()))).get().center, getCenter());

        // Auswahl der bereich die in etwa den gleichen abstand zum mittelpunkt haben
        val prozentualeAbweichung = 0.2;
        for (RotatedRect r : textAreas.stream().filter(rotatedRect -> Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()) / maxDist > (1 - prozentualeAbweichung)).collect(Collectors.toList())) {
            try {
                BufferedImage sub = Helper.Mat2BufferedImage(getRefittetSrcMat().submat(r.boundingRect()));
                String str = textDedection.doOCRNumbers(sub).replaceAll("[^0-9]", "");
                Integer i = Integer.parseInt(str);
                if (!steps.isPresent() || i % steps.get() == 0 ) {
                    labels.put(r, i);
                }
            } catch (Exception e) {
            }

        }


//        val drawing = getGaugeMat().clone();
//        for (int i = 0; i < contours.size(); i++) {
//            Imgproc.drawContours(drawing,contours,i,getColor());
//        }
//        HighGui.imshow("Cont", drawing);
//        HighGui.waitKey();
    }

    public Map<RotatedRect, Integer> getLabels() {
        return labels;
    }
}