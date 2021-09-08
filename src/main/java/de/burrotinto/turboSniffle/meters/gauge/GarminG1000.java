package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.HeatMap;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

    private Rect altimeterUPRect = new Rect(new Point(723, 113), new Point(788, 250));
    private Rect altimeterDOWNRect = new Rect(new Point(723, 318), new Point(788, 455));
    private Rect altimeter = new Rect(new Point(altimeterUPRect.x, altimeterUPRect.y),
            new Point(altimeterDOWNRect.x + altimeterDOWNRect.width, altimeterDOWNRect.y + altimeterDOWNRect.height));

    private Textarea altimeterUP = new Textarea(null, altimeterUPRect, null, false);
    private Textarea altimeterDOWN = new Textarea(null, altimeterDOWNRect, null, false);

    private int centerLine = 284;

    @SneakyThrows
    public GarminG1000(Mat src) {
        g1000TextOptimiert = new Mat();
        g1000 = new Mat();

        Imgproc.resize(src, g1000TextOptimiert, SIZE);
        Imgproc.resize(src, g1000, SIZE);

        //Init textOptimierteVersion
        textDedection.addOptions("tessedit_char_whitelist", "01234567890");
        g1000TextOptimiert = getBestThreshAndMat(g1000TextOptimiert).p2;

        val cannyEdgeDetector = GaugeFactory.getCanny();
        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(g1000));
        cannyEdgeDetector.process();

        HeatMap heatMap = new HeatMap(cannyEdgeDetector.getEdgeMat().submat(KURSKREISEL_POS));
        pot = new Point(KURSKREISEL_POS.x + heatMap.getCenter().x, KURSKREISEL_POS.y + heatMap.getCenter().y);

        Imgproc.drawMarker(g1000, pot, Helper.BLACK, 0, 1000);


        getKandidatenFuerAltimeter(g1000TextOptimiert).forEach(rectDoublePair -> {
            Imgproc.rectangle(g1000, rectDoublePair.p1, Helper.BLACK, -1);
            Imgproc.putText(g1000, rectDoublePair.p2 + "", new Point(rectDoublePair.p1.x, rectDoublePair.p1.y + rectDoublePair.p1.height - 2), 0, 0.5, Helper.WHITE);

        });

        val sorted = getKandidatenFuerAltimeter(g1000TextOptimiert);
        double xPP = Math.abs(sorted.get(3).p2 - sorted.get(0).p2) / (Math.abs(sorted.get(3).p1.y + (sorted.get(3).p1.height * 0.5)) - (sorted.get(0).p1.y + (sorted.get(0).p1.height * 0.5)));
        double x = sorted.get(0).p2 + (xPP * (centerLine - sorted.get(0).p1.y - sorted.get(0).p1.height * 0.5));

        System.out.println("Altimeter " + x);
        HighGui.imshow("", g1000);
        HighGui.waitKey();
    }

    /**
     * Sortierte Rückgabe
     *
     * @param mat
     * @return
     */
    public List<Pair<Rect, Double>> getKandidatenFuerAltimeter(Mat mat) {
        val numbers = new ArrayList<>(getAllNumbersOfArea(mat, altimeterUPRect, 100));
        numbers.addAll(new ArrayList<>(getAllNumbersOfArea(mat, altimeterDOWNRect, 100)));
//        return numbers.stream().sorted((o1, o2) -> o2.p2.compareTo(o1.p2)).findFirst().get();
        return numbers.stream().sorted(Comparator.comparing(o -> o.p2)).collect(Collectors.toList());
    }


    private List<Pair<Rect, Double>> getAllNumbersOfArea(Mat src, Rect area, double mod) {
        return textDedection.getTextAreasWithTess(src.submat(area)).stream().map(rotatedRect -> {
            Rect rect = new Rect(new Point(rotatedRect.boundingRect().x + area.x, rotatedRect.boundingRect().y + area.y),
                    (new Point(rotatedRect.boundingRect().x + area.x + rotatedRect.boundingRect().width, rotatedRect.boundingRect().y + area.y + rotatedRect.boundingRect().height)));
            return new Pair<>(rect, parseDoubleOrNAN(textDedection.doOCRNumbers(src.submat(rect))));
        }).filter(rectDoublePair -> rectDoublePair.p2 % mod == 0).collect(Collectors.toList());
    }

    public Pair<Integer, Mat> getBestThreshAndMat(Mat src) {
        Mat w = new Mat();
        int thresh = 205;
        int nmbrsCount = 0;
        boolean isReady = false;

        Pair<Integer, Mat> out = null;
        do {
            thresh -= 5;
            Imgproc.threshold(src, w, thresh, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(w, w);

            if (!altimeterUP.isInit) {
                val altiUp = getAllNumbersOfArea(w, altimeterUP.origin, 100);
                if (!altimeterUP.isInit && Helper.berecheDieDurchschnittlicheVeränderungDesDatensatzes(altiUp.stream().map(rectDoublePair -> rectDoublePair.p2).collect(Collectors.toList())) == -100.0) {
                    altimeterUP.area = w.submat(altimeterUP.origin).clone();
                    altimeterUP.textFields = altiUp;
                    altimeterUP.isInit = true;
                }
            }
            if (!altimeterDOWN.isInit) {
                val altiDown = getAllNumbersOfArea(w, altimeterDOWN.origin, 100);
                if (Helper.berecheDieDurchschnittlicheVeränderungDesDatensatzes(altiDown.stream().map(rectDoublePair -> rectDoublePair.p2).collect(Collectors.toList())) == -100.0) {
                    altimeterDOWN.area = w.submat(altimeterDOWN.origin).clone();
                    altimeterDOWN.textFields = altiDown;
                    altimeterDOWN.isInit = true;
                }
            }

//            HighGui.imshow("", w);
//            HighGui.waitKey(100);

            if (altimeterUP.isInit && altimeterDOWN.isInit) {
                out = new Pair<>(thresh, w);
                altimeterUP.area.copyTo(out.p2.submat(altimeterUP.origin));
                altimeterDOWN.area.copyTo(out.p2.submat(altimeterDOWN.origin));
                isReady = true;
            }

        } while (!isReady && thresh > 50);

        HighGui.imshow("", out.p2);
        HighGui.waitKey();
        return out;

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

    @AllArgsConstructor
    private class Textarea {
        Mat area;
        Rect origin;
        List<Pair<Rect, Double>> textFields;
        boolean isInit;
    }
}
