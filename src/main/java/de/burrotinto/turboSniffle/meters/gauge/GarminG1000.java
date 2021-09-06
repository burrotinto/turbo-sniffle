package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.TextDedection;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class GarminG1000 {
    //Bei einem 12" Display ist die Länge 9,6" und die höhe 7,2" entsprich 106,666 DPI
    public static final Size SIZE = new Size(1024, 768);
    private static TextDedection textDedection = new TextDedection(TextDedection.ENGRESTRICT_BEST_INT, 107);
    private final Mat g1000;


    private final static Rect altimeter = new Rect(new Point(700, 70), new Point(810, 450));

    @SneakyThrows
    public GarminG1000(Mat src) {
        g1000 = new Mat();
        Imgproc.resize(src, g1000, SIZE);
//        textDedection.addOptions("psm", "6");
//        textDedection.addOptions("oem", "0");
        textDedection.addOptions("tessedit_char_whitelist", "01234567890");
//        int i = 50;
//        while (i <= SIZE.height || i <= SIZE.width) {
//            if (i <= SIZE.height) {
//                Imgproc.line(g1000, new Point(0, i), new Point(SIZE.width, i), Helper.BLACK);
//            }
//            if (i <= SIZE.width) {
//                Imgproc.line(g1000, new Point(i, 0), new Point(i, SIZE.height), Helper.BLACK);
//            }
//            i += 50;
//        }

        Imgproc.threshold(g1000, g1000, 0, 255, Imgproc.THRESH_OTSU);
        Core.bitwise_not(g1000, g1000);
        System.out.println(textDedection.doOCRNumbers(g1000.submat(altimeter)));

//        Imgproc.rectangle(g1000, altimeter, Helper.BLACK);


        val cannyEdgeDetector = GaugeFactory.getCanny();
        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(g1000));
        cannyEdgeDetector.process();

        Map<RotatedRect, Mat> map = new HashMap<>();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyEdgeDetector.getEdgeMat(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        HighGui.imshow("canny", cannyEdgeDetector.getEdgeMat());
//        HighGui.waitKey();

        for (int i = 0; i < contours.size(); i++) {
            val rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));

            //Nur Konturen die eine Mindestgröße besitzen
            if (rect.size.area() > 10000) {
//                Mat xxx = transformieren(g1000,rect,g1000.size());
                try {
                    HighGui.imshow(i + "", g1000.submat(rect.boundingRect()));
                } catch (Exception e) {

                }
            }
        }


        val kurskreisel = Cessna172SixpackFactory.getCessna172Kurskreisel(g1000);
        HighGui.imshow("", g1000);
        HighGui.imshow("kurs", kurskreisel.getDrawing(kurskreisel.source));
        HighGui.waitKey();
    }


    public static Mat transformieren(Mat mat, RotatedRect ellipse, Size size) {
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transformieren(mat, Arrays.stream(pts).collect(Collectors.toList()), size);
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
}
