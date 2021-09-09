package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.HeatMap;
import de.burrotinto.turboSniffle.meters.gauge.impl.Pixel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.sourceforge.lept4j.Pix;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GarminG1000 {
    //Bei einem 12" Display ist die Länge 9,6" und die höhe 7,2" entsprich 106,666 DPI
    public static final Size SIZE = new Size(1024, 768);
    public static final Point POINT_OF_TRUST = new Point(460, 586);
    private static final Rect KURSKREISEL_POS = new Rect(new Point(252, 335), new Point(678, 747));
    private static TextDedection textDedection = new TextDedection(TextDedection.ENGRESTRICT_BEST_INT, 107);
    private Mat g1000, g1000TextOptimiert;
    private Point pot;

    private int centerLine = 284;

    private Textarea altimeterUP = new Textarea(new Rect(new Point(723, 113), new Point(808, 250)));
    private Textarea altimeterDOWN = new Textarea(new Rect(new Point(723, 320), new Point(808, 455)));
//    private Textarea altimeter = new Textarea(new Rect(new Point(altimeterUP.origin.x, altimeterUP.origin.y),
//            new Point(altimeterDOWN.origin.x + altimeterDOWN.origin.width, altimeterDOWN.origin.y + altimeterDOWN.origin.height)));

    private Textarea airspeedUP = new Textarea(new Rect(new Point(160, 120), new Point(225, 250)));
    private Textarea airspeedDOWN = new Textarea(new Rect(new Point(160, 320), new Point(225, 455)));

    private TextField kurskreisel = new TextField(new Rect(new Point(425, 401), new Point(495, 435)), 0.0, 360.0);

    private Rect verticalSpeedIndicator = new Rect(new Point(810, 144), new Point(830, 424));

    @SneakyThrows
    public GarminG1000(Mat src) {
        g1000TextOptimiert = new Mat();
        g1000 = new Mat();

        Imgproc.resize(src, g1000TextOptimiert, SIZE);
        Imgproc.resize(src, g1000, SIZE);

        val g = Cessna172SixpackFactory.getCessna172Kurskreisel(g1000.submat(KURSKREISEL_POS));
        Imgcodecs.imwrite("data/out/g100Kurskreisel.png", g.otsu);
        System.out.println(g.getValue());
        HighGui.imshow("Kurs",g.getDrawing(g.source));
        HighGui.waitKey();
        //Init textOptimierteVersion
        textDedection.addOptions("tessedit_char_whitelist", "01234567890");
        getBestThreshAndMat(g1000TextOptimiert);

        //POT finden
//        val cannyEdgeDetector = GaugeFactory.getCanny();
//        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(g1000));
//        cannyEdgeDetector.process();
//        HeatMap heatMap = new HeatMap(cannyEdgeDetector.getEdgeMat().submat(KURSKREISEL_POS));
//        pot = new Point(KURSKREISEL_POS.x + heatMap.getCenter().x, KURSKREISEL_POS.y + heatMap.getCenter().y);
//
//        //POT MARKIEREN
//        Imgproc.drawMarker(g1000, pot, Helper.BLACK, 0, 1000);


//        Imgproc.line(g1000, new Point(0, centerLine), new Point(g1000.width(), centerLine), Helper.WHITE);



        System.out.println("Alti = " + getAltimeter());
        System.out.println("Airspeed = " + getAirspeed());
        System.out.println("Kurskreisel = " + getKurskreisel());
        System.out.println("VSI =" + getVSI());
        HighGui.imshow("", g1000);
        HighGui.waitKey();
    }

    /**
     * Gibt den Angezeigten Wert des Altimeters aus
     *
     * @return
     */
    public double getAltimeter() {
        val alti = new ArrayList<>(altimeterDOWN.textFields);
        alti.addAll(altimeterUP.textFields);
        double x = Helper.berecheDieDurchschnittlicheVeränderungDesDatensatzes(alti.stream().map(rectDoublePair -> rectDoublePair.p2).collect(Collectors.toList()));
        if (x != -100 || alti.size() <= 2) {
            return ((Helper.getCenter(alti.get(0).p1).y - centerLine) * (100 / 57.0)) + alti.get(0).p2;
        }
        return (calculateVale(alti));
    }

    public double getAirspeed() {
        val airs = new ArrayList<>(airspeedUP.textFields);
        airs.addAll(airspeedDOWN.textFields);
        if (airs.size() == 1) {
            return ((Helper.getCenter(airs.get(0).p1).y - centerLine) * (10.0 / 57.0)) + airs.get(0).p2;
        }
        return (calculateVale(airs));
    }

    public double getKurskreisel() {
        return kurskreisel.fieldValue;
    }

    public double getVSI() {
        val x = Helper.getAllPixel(g1000.submat(verticalSpeedIndicator));
        x.sort(Pixel::compareTo);
        int darkest = x.get(0).color;
        ArrayList<Pixel> darkPixels = new ArrayList<>(x.stream().filter(pixel -> pixel.color <= darkest * 1.2).collect(Collectors.toList()));
//        val pixelIterator = darkPixels.iterator();
//        int i = 0;
//        Pixel p = null;
//        while (pixelIterator.hasNext() && (p.color <= darkest * 1.2){
//            i++;
//            p = pixelIterator.next();
//
//        }
        double y = 0;
        for (int i = 0; i < darkPixels.size(); i++) {
            y += darkPixels.get(i).point.y;
        }

        y = y / darkPixels.size();

//        System.out.println(verticalSpeedIndicator.height);
//        System.out.println(y);
//        System.out.println(4000.0 / verticalSpeedIndicator.height);
//        System.out.println();
        return (2000.0 / 140.0) * ((verticalSpeedIndicator.height * 0.5) - y);
    }

    private List<Pair<Rect, Double>> getAllNumbersOfArea(Mat src, Rect area, double mod) {
        val x = textDedection.getTextAreasWithTess(src.submat(area)).stream().map(rotatedRect -> {
            Rect rect = new Rect(new Point(rotatedRect.boundingRect().x + area.x, rotatedRect.boundingRect().y + area.y),
                    (new Point(rotatedRect.boundingRect().x + area.x + rotatedRect.boundingRect().width, rotatedRect.boundingRect().y + area.y + rotatedRect.boundingRect().height)));
            return new Pair<>(rect, parseDoubleOrNAN(textDedection.doOCRNumbers(src.submat(rect))));
        }).collect(Collectors.toList());
        return x.stream().filter(rectDoublePair -> rectDoublePair.p2 % mod == 0).collect(Collectors.toList());
    }

    public void getBestThreshAndMat(Mat src) {
        Mat w = new Mat();
        int thresh = 205;
        boolean isReady = false;

        Pair<Integer, Mat> out = null;
        do {
            thresh -= 5;
            Imgproc.threshold(src, w, thresh, 255, Imgproc.THRESH_BINARY);
            if (Core.countNonZero(w) != w.size().area()) {
                Core.bitwise_not(w, w);

                initTextarea(w, altimeterUP, 100.0);
                initTextarea(w, altimeterDOWN, 100.0);

                initTextarea(w, airspeedUP, 10.0);
                initTextarea(w, airspeedDOWN, 10.0);

                initTextField(w, kurskreisel);

                HighGui.imshow("", w);
                HighGui.waitKey(100);
                if ((altimeterUP.isInit && altimeterDOWN.isInit && airspeedUP.isInit && airspeedDOWN.isInit) || Core.countNonZero(w) == 0) {
//                    altimeterUP.area.copyTo(out.p2.submat(altimeterUP.origin));
//                    altimeterDOWN.area.copyTo(out.p2.submat(altimeterDOWN.origin));
                    isReady = true;
                }
            }
        } while (!isReady && thresh > 50);
//        out = new Pair<>(thresh, w);
//        return out;

    }

    private void initTextField(Mat w, TextField textField) {
        if (!textField.isInit) {
            val v = parseDoubleOrNAN(textDedection.doOCRNumbers(w.submat(textField.origin)));
            if (!Double.isNaN(v) && textField.min <= v && textField.max > v) {
                textField.fieldValue = v;
                textField.isInit = true;
            }
        }
    }

    private void initTextarea(Mat w, Textarea textarea, double step) {
        if (!textarea.isInit) {
            val x = getAllNumbersOfArea(w, textarea.origin, step);
            if (Helper.berecheDieDurchschnittlicheVeränderungDesDatensatzes(x.stream().map(rectDoublePair -> rectDoublePair.p2).collect(Collectors.toList())) == -step) {
                textarea.area = w.submat(textarea.origin).clone();
                textarea.textFields = x;
                textarea.isInit = true;
            } else if (x.size() == 1 && textarea.textFields.isEmpty()) {
                textarea.textFields.addAll(x);
            }
        }
    }

    private double calculateVale(List<Pair<Rect, Double>> l) {
        if (l.size() <= 1) {
            return Double.NaN;
        } else {
            double sum = 0;
            int anz = 0;
            for (int i = 0; i < l.size() - 1; i++) {
                for (int j = i + 1; j < l.size(); j++) {
                    sum += calculateValeInRelationToCenterLine(l.get(i), l.get(j));
                    anz++;
                }
            }
            return sum / anz;
        }
    }

    private double calculateValeInRelationToCenterLine(Pair<Rect, Double> a, Pair<Rect, Double> b) {
        //berechnung wieviel ein Pixel an wert hat
        double xPP = (a.p2 - b.p2) / (Helper.getCenter(a.p1).y - Helper.getCenter(b.p1).y);
        return (centerLine - Helper.getCenter(b.p1).y) * xPP + b.p2;
    }

    public static Mat transformieren(Mat mat, List<Point> corner, Size size) {

        List<Point> target = new ArrayList<Point>();
        target.add(new Point(0, 0));
        target.add(new Point(size.width, 0));
        target.add(new Point(size.width, size.height));
        target.add(new Point(0, size.height));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);

        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, size);

        return proj;
    }

    public static Double parseDoubleOrNAN(String string) {
        try {
            return Double.parseDouble(string);
        } catch (Exception e) {
            return Double.NaN;
        }
    }


    @RequiredArgsConstructor
    private class Textarea {
        Mat area = new Mat();
        @NonNull
        Rect origin;
        List<Pair<Rect, Double>> textFields = new ArrayList<>();
        boolean isInit = false;
    }

    @RequiredArgsConstructor
    private class TextField {
        @NonNull
        Rect origin;
        Double fieldValue = Double.NaN;
        @NonNull
        Double min, max;
        boolean isInit = false;

    }
}
