package de.burrotinto.turboSniffle.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import lombok.*;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GarminG1000 {
    //Das Display besitzt 123DPI, Tesseract liefert die Besten ergebnisse bei 300 DPI darm faktor 3 skalieren
    public static final Size SIZE = new Size(skale(1024), skale(768));
    public static final Point POINT_OF_TRUST = new Point(skale(460), skale(586));
    private static final Rect KURSKREISEL_POS = new Rect(new Point(skale(252), skale(335)), new Point(skale(678), skale(747)));
    private static TextDedection textDedection = new TextDedection(TextDedection.ENGRESTRICT_BEST_INT, skale(123));

    private Mat g1000, g1000SRC;
    private Point pot;

    private int centerLine = skale(284);

    private Textarea altimeterUP = new Textarea(new Rect(new Point(skale(723), skale(115)), new Point(skale(808), skale(250))));
    private Textarea altimeterDOWN = new Textarea(new Rect(new Point(skale(723), skale(320)), new Point(skale(808), skale(455))));

    private Textarea airspeedUP = new Textarea(new Rect(new Point(skale(160), skale(115)), new Point(skale(220), skale(250))));
    private Textarea airspeedDOWN = new Textarea(new Rect(new Point(skale(160), skale(320)), new Point(skale(220), skale(455))));

    private TextField kurskreisel = new TextField(new Rect(new Point(skale(425), skale(402)), new Point(skale(480), skale(432))));

    private int vsiHeight = 280;
    private Textarea vsi = new Textarea(new Rect(new Point(skale(830), skale(127)), new Point(skale(875), skale(440))));
    private Rect verticalSpeedIndicator = new Rect(new Point(skale(810), centerLine - skale(vsiHeight / 2)), new Point(skale(850), centerLine + skale(vsiHeight / 2)));

    @SneakyThrows
    public GarminG1000(Mat src) {

        textDedection.addOptions("tessedit_char_whitelist", "01234567890");

        g1000SRC = new Mat();
        g1000 = new Mat();

        //Schritte win in BAchelorarbeit beschrieben zur optimierung der Texterkennung:
        // Skalieren
        Imgproc.resize(src, g1000SRC, SIZE);
        Imgproc.resize(src, g1000, SIZE);

//        Imgcodecs.imwrite("data/out/G1000GreyundRescale.png", g1000);

        Imgproc.createCLAHE(1.0, new Size(24, 24)).apply(g1000, g1000);
//        Imgcodecs.imwrite("data/out/G1000CLAHE.png", g1000);

        Imgproc.threshold(g1000, g1000, 0, 255, Imgproc.THRESH_OTSU);
//        Imgcodecs.imwrite("data/out/G1000OTSU.png", g1000);

        Photo.fastNlMeansDenoising(g1000, g1000);
//        Imgcodecs.imwrite("data/out/G1000denoise.png", g1000);

        g1000 = Helper.erode(g1000, Imgproc.CV_SHAPE_RECT, 2);
//        Imgcodecs.imwrite("data/out/G1000ERODE.png", g1000);

        Core.bitwise_not(g1000, g1000);
//        Imgcodecs.imwrite("data/out/G1000NOT.png", g1000);


        initTextarea(g1000, altimeterUP);
        initTextarea(g1000, altimeterDOWN);

        initTextarea(g1000, airspeedUP);
        initTextarea(g1000, airspeedDOWN);

        initTextField(g1000, kurskreisel);

        initTextarea(g1000, vsi);


//        System.out.println("Alti = " + getAltimeter());
//        System.out.println("Airspeed = " + getAirspeed());
//        System.out.println("Kurskreisel = " + getKurskreisel());
//        System.out.println("VSI =" + getVSI());
//
//
//        drawArea(g1000, airspeedUP);
//        drawArea(g1000, airspeedDOWN);
//        drawArea(g1000, altimeterUP);
//        drawArea(g1000, altimeterDOWN);
//        drawArea(g1000, vsi);
//
//        drawTf(g1000, kurskreisel);
////        Imgproc.rectangle(g1000, altimeterUP.origin, Helper.GREY, 3);
////        Imgproc.rectangle(g1000, altimeterDOWN.origin, Helper.GREY, 3);
////        Imgproc.rectangle(g1000, verticalSpeedIndicator, Helper.GREY, 3);
////        Imgproc.rectangle(g1000, airspeedUP.origin, Helper.GREY, 3);
////        Imgproc.rectangle(g1000, airspeedDOWN.origin, Helper.GREY, 3);
////        Imgproc.rectangle(g1000, kurskreisel.origin, Helper.GREY, 3);
//        Imgproc.line(g1000, new Point(0, centerLine), new Point(g1000.cols(), centerLine), Helper.GREY, 10);
//
//        Imgproc.resize(g1000, g1000, new Size(1024, 768));
//        HighGui.imshow("", g1000);
//        HighGui.waitKey();
    }

    public Mat getOCROptimiert(){
        return g1000.clone();
    }
    private void drawTf(Mat draw, TextField tf) {
        Imgproc.rectangle(draw, tf.origin, Helper.WHITE, -1);
        Imgproc.rectangle(draw, tf.origin, Helper.GREY, 2);
        Imgproc.putText(draw, String.valueOf(Precision.round(tf.fieldValue, 0)), new Point(tf.origin.x, tf.origin.y + (tf.origin.height - 10)), Imgproc.FONT_HERSHEY_TRIPLEX, 1.2, Helper.BLACK);
    }

    private void drawArea(Mat draw, Textarea ta) {
        ta.textFields.forEach(textField -> drawTf(draw, textField));
    }

    /**
     * Gibt den Angezeigten Wert des Altimeters aus
     *
     * @return
     */
    public double getAltimeter() {
        List<TextField> alti = new ArrayList<>(altimeterDOWN.textFields);
        alti.addAll(altimeterUP.textFields);

        alti = alti.stream().filter(textField -> textField.isInit).collect(Collectors.toList());
        Collections.shuffle(alti);

        double pp = downSkale(100 / 57.0);
//
//        if (alti.size() > 1) {
//
//            pp = calcValuePerPixel(alti.get(0), alti.get(1));
//        }

        double[] values = new double[alti.size()];

        for (int i = 0; i < alti.size(); i++) {
            double delta = Helper.getCenter(alti.get(i).origin).y - centerLine - skale(1);
            values[i] = alti.get(i).fieldValue + (delta * (pp));
        }

        return getMinDistEntryToMean(values);
    }

    public double getAirspeed() {
        val airs = new ArrayList<>(airspeedUP.textFields);
        airs.addAll(airspeedDOWN.textFields);

        double[] values = new double[airs.size()];

        for (int i = 0; i < airs.size(); i++) {
            values[i] = ((Helper.getCenter(airs.get(i).origin).y - centerLine) * downSkale(10 / 57.0)) + airs.get(i).fieldValue;
        }

        return getMinDistEntryToMean(values);
    }

    private double calcValuePerPixel(TextField a, TextField b) {
        val aC = a.origin.y + (a.origin.height / 2);
        val bC = b.origin.y + (b.origin.height / 2);

        return Math.abs(b.fieldValue - a.fieldValue) / Math.abs(aC - bC);
    }

    /**
     * Ohne NAN
     *
     * @param in
     * @return
     */
    private double getMinDistEntryToMean(double[] in) {
        double[] values = Arrays.stream(in).filter(value -> !Double.isNaN(value)).toArray();
        Double mean = Arrays.stream(values).sum() / values.length;

        double value = values[0];
        for (int i = 0; i < values.length; i++) {
            if (Math.abs(value - mean) > Math.abs(values[i] - mean)) {
                value = values[i];
            }
        }
        return value;
    }

    public double getKurskreisel() {
        return kurskreisel.fieldValue;
    }

    public double getVSI() {
        vsi.textFields = vsi.textFields.stream().filter(textField -> textField.fieldValue % 50 == 0 && textField.fieldValue != 0).collect(Collectors.toList());

        if (vsi.textFields.size() == 1) {

            return ((centerLine - vsi.textFields.get(0).origin.y) > 0? 1 : -1) * vsi.textFields.get(0).fieldValue ;
        } else if (vsi.textFields.isEmpty()) {
            return 0;
        } else {
            return Double.NaN;
        }
    }

    public double getVSI2() {
        val sub = g1000SRC.submat(verticalSpeedIndicator);
        val result = new int[sub.rows() - 10];

        for (int row = 5; row < sub.rows() - 5; row++) {
            int sum = 0;
            for (int col = sub.cols() / 2; col < sub.cols(); col++) {
                for (int i = -5; i <= 5; i++) {
                    sum += (int) sub.get(i + row, col)[0];
                }
            }
            result[row - 5] += sum;
        }

        int centerOfVSI = 0;
        for (int i = 1; i < result.length; i++) {
            if (result[centerOfVSI] > result[i]) {
                centerOfVSI = i;
            }
        }

        Rect r = new Rect(verticalSpeedIndicator.x, verticalSpeedIndicator.y + 5 + centerOfVSI - 50, 200, 100);


        TextField vsi = new TextField(r);
        initTextField(g1000, vsi);
        if (vsi.fieldValue.isNaN()) {
            vsi.fieldValue = (((double) (centerOfVSI - (result.length / 2)) / (result.length / 2)) * (2000 / (result.length / 2)));
        }

        return vsi.fieldValue;
    }


    private List<TextField> getAllNumbersOfArea(Mat src, Rect area) {
        val x = textDedection.getTextAreasWithTess(src.submat(area)).stream().map(rotatedRect -> {
            // Gefundenen Textstellen aud Gesamtbild beziehen
            Rect rect = new Rect(new Point(rotatedRect.boundingRect().x + area.x, rotatedRect.boundingRect().y + area.y),
                    (new Point(rotatedRect.boundingRect().x + area.x + rotatedRect.boundingRect().width, rotatedRect.boundingRect().y + area.y + rotatedRect.boundingRect().height)));

            TextField tv = new TextField(rect);
            initTextField(src, tv);

            return tv;
        }).collect(Collectors.toList());
        return x;
    }


    private void initTextField(Mat w, TextField textField) {
        val v = Helper.parseDoubleOrNAN(textDedection.doOCRNumbers(w.submat(textField.origin)));
        textField.fieldValue = v;
        textField.isInit = !v.isNaN();

    }


    private void initTextarea(Mat w, Textarea textarea) {

        if (!textarea.isInit) {
            Imgcodecs.imwrite("data/out/G1000_" + textarea.origin.toString() + ".png", Helper.sharpen(w.submat(textarea.origin)));

            val x = getAllNumbersOfArea(w, textarea.origin);
            textarea.textFields.addAll(x);

            textarea.isInit = textarea.textFields.stream().allMatch(textField -> textField.isInit);

        }
    }

    @RequiredArgsConstructor
    private class Textarea {
        Mat area = new Mat();
        @NonNull
        Rect origin;
        List<TextField> textFields = new ArrayList<>();
        boolean isInit = false;
    }

    @RequiredArgsConstructor
    private class TextField {
        @NonNull
        Rect origin;
        Double fieldValue = Double.NaN;

        boolean isInit = false;

    }

    private static int skale(int org) {
        return org * 3;
    }

    private static double skale(double org) {
        return org * 3;
    }

    private static int downSkale(int org) {
        return org / 3;
    }

    private static double downSkale(double org) {
        return org / 3;
    }

}
