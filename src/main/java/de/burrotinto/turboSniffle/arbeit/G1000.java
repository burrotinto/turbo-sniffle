package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class G1000 implements Arbeit {
    private static final String G1000 = "data/ae/G1000/GARMIN.png";
    private static final String TEST = "data/ae/G1000/als-sim-al42-photo-alsim-al42-simulateur-04.jpg";
    private static final int BILATERAL_D = 20;

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

        new G1000().machDeinDing();
    }

    @Override
    public void machDeinDing() {
        Mat g1000 = Imgcodecs.imread(G1000, Imgcodecs.IMREAD_GRAYSCALE);
        Mat x = Imgcodecs.imread(TEST, Imgcodecs.IMREAD_GRAYSCALE);

        val tx = new TextDedection("",300);
        tx.getTextAreasWithTess(x).stream().filter(rotatedRect -> rotatedRect.size.area() > 50).forEach(rotatedRect -> {
            try {
                BufferedImage sub = Helper.Mat2BufferedImage(x.submat(rotatedRect.boundingRect()));
                val t = tx.doOCRNumbers(sub);
                System.out.println(t);
                HighGui.imshow(t,x.submat(rotatedRect.boundingRect()));
                HighGui.waitKey(1);
                if (t.contains("OBS")) {
                    System.out.println(t);
                    HighGui.imshow("OBS", x.submat(rotatedRect.boundingRect()));
                    HighGui.waitKey();
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }

        });
//        Mat bilateral = new Mat();
//        Imgproc.bilateralFilter(x, bilateral, BILATERAL_D, BILATERAL_D * 2.0, BILATERAL_D * 0.5);
//
//        Mat otsu = new Mat();
//        Imgproc.threshold(bilateral, otsu, 0, 255, Imgproc.THRESH_OTSU);
//
//
//        Mat canny = new Mat();
//        Imgproc.Canny(bilateral, canny, 85, 120);
//
//        val cannyEdgeDetector =  GaugeFactory.getCanny();
//        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(x));
//        cannyEdgeDetector.process();
//
//        Map<RotatedRect, Mat> map = new HashMap<>();
//        ArrayList<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(cannyEdgeDetector.getEdgeMat(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        HighGui.imshow("bi", bilateral);
//        HighGui.imshow("otsu", otsu);
//        HighGui.imshow("canny", cannyEdgeDetector.getEdgeMat());
//        HighGui.waitKey();
//
//        for (int i = 0; i < contours.size(); i++) {
//            val rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
//
//            //Nur Konturen die eine Mindestgröße besitzen
//            if (rect.size.area() > 10000) {
//                Mat xxx = transformieren(x,rect,g1000.size());
//
//                HighGui.imshow(xxx.toString(),xxx);
//            }
//
//        }
//        HighGui.waitKey();
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

