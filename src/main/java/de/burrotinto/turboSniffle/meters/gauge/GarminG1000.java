package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.Pair;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.impl.HeatMap;
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

    private Rect altimeterUP = new Rect(new Point(723, 113), new Point(788, 250));
    private Rect altimeterDOWN = new Rect(new Point(723, 318), new Point(788, 455));
    private Rect altimeter = new Rect(new Point(altimeterUP.x, altimeterUP.y),
            new Point(altimeterDOWN.x + altimeterDOWN.width, altimeterDOWN.y + altimeterDOWN.height));

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
        val numbers = getAllNumbersOfArea(mat, altimeterUP).stream().filter(rectDoublePair -> rectDoublePair.p2 % 100 == 0).collect(Collectors.toList());
        numbers.addAll(getAllNumbersOfArea(mat, altimeterDOWN).stream().filter(rectDoublePair -> rectDoublePair.p2 % 100 == 0).collect(Collectors.toList()));
//        return numbers.stream().sorted((o1, o2) -> o2.p2.compareTo(o1.p2)).findFirst().get();
        return numbers.stream().sorted(Comparator.comparing(o -> o.p2)).collect(Collectors.toList());
    }


    private List<Pair<Rect, Double>> getAllNumbersOfArea(Mat src, Rect area) {
        return textDedection.getTextAreasWithTess(src.submat(area)).stream().map(rotatedRect -> {
            Rect rect = new Rect(new Point(rotatedRect.boundingRect().x + area.x, rotatedRect.boundingRect().y + area.y),
                    (new Point(rotatedRect.boundingRect().x + area.x + rotatedRect.boundingRect().width, rotatedRect.boundingRect().y + area.y + rotatedRect.boundingRect().height)));
            return new Pair<>(rect, parseDoubleOrNAN(textDedection.doOCRNumbers(src.submat(rect))));
        }).collect(Collectors.toList());
    }

    public Pair<Integer, Mat> getBestThreshAndMat(Mat src) {
        Mat w = new Mat();
        int thresh = 200;
        int nmbrsCount = 0;
        boolean isReady = false;

        Pair<Integer, Mat> out = null;
        do {
            Imgproc.threshold(src, w, thresh, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(w, w);

            val altimeter = getKandidatenFuerAltimeter(w).stream().sorted((o1, o2) -> o2.p2.compareTo(o1.p2)).collect(Collectors.toList());

            HighGui.imshow("", w);
            HighGui.waitKey(100);
            altimeter.forEach(rectDoublePair -> System.out.println(rectDoublePair.p2));
            System.out.println();
            if (altimeter.size() >= 4 && altimeter.get(0).p2 - altimeter.get(1).p2 == 100 && altimeter.get(2).p2 - altimeter.get(3).p2 == 100) {
                out = new Pair<>(thresh, w.clone());
                isReady = true;
            }
//            if (nmbrsCount < numbers.length) {
//                nmbrsCount = numbers.length;
//                out = new Pair<>(thresh, w.clone());
//            } else if (nmbrsCount > numbers.length) {
//                isReady = true;
//            }
            thresh -= 5;
        } while (!isReady);
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
}
