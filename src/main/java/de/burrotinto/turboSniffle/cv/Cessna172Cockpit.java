package de.burrotinto.turboSniffle.cv;

import de.burrotinto.popeye.transformation.Pair;
import lombok.val;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Cessna172Cockpit extends DisplayMainFrame {
    public Cessna172Cockpit(Mat mat) {
        val drawing = Mat.zeros(mat.size(), mat.type());
        setMainframe(mat);

        int threshold = 120;
        val cannyOutput = mat.clone();

        Imgproc.Canny(getGreyMainframe(), cannyOutput, threshold, threshold * 2);

        HighGui.imshow("canny", cannyOutput);


        val contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();


        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        MatOfPoint2f approxCurve = new MatOfPoint2f();


//        for (int idx = 0; idx <  contours.size(); idx++) {
//            MatOfPoint contour = contours.get(idx);
//            Rect rect = Imgproc.boundingRect(contour);
//            Imgproc.drawContours(drawing, contours, idx, new Scalar(255, 255, 255));
//            double contourArea = Imgproc.contourArea(contour);
//            matOfPoint2f.fromList(contour.toList());
//            Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
//            long total = approxCurve.total();
//            if (total == 3) { // is triangle
//                // do things for triangle
//            }
//            if (total >= 4 && total <= 6) {
//                List<Double> cos = new ArrayList<>();
//                Point[] points = approxCurve.toArray();
//                for (int j = 2; j < total + 1; j++) {
//                    cos.add(Helper.angle(points[(int) (j % total)], points[j - 2], points[j - 1]));
//                }
//                Collections.sort(cos);
//                Double minCos = cos.get(0);
//                Double maxCos = cos.get(cos.size() - 1);
//                boolean isRect = total == 4 && minCos >= -0.1 && maxCos <= 0.3;
//                boolean isPolygon = (total == 5 && minCos >= -0.34 && maxCos <= -0.27) || (total == 6 && minCos >= -0.55 && maxCos <= -0.45);
//
//                if (isRect) {
//                    double ratio = Math.abs(1 - (double) rect.width / rect.height);
//                    drawText(drawing, rect.tl(), ratio <= 0.02 ? "SQU" : "RECT");
//                }
//                if (isPolygon) {
//                    drawText(drawing, rect.tl(), "Polygon");
//                }
//            }
//
//        }
//        val lines = getLineDedection(cannyOutput);
//        for (int x = 0; x < lines.rows(); x++) {
//            double[] l = lines.get(x, 0);
//            Imgproc.line(drawing, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255,255, 255), 1, Imgproc.LINE_AA, 0);
//        }
//        HighGui.imshow("aa", drawing);
//


//        val list =  getCircels(getGreyMainframe());
//
//        val a = textDedection.getTextAreas(getGreyMainframe());
//        a.forEach(rotatedRect ->{
//            val x = textDedection.doOCR(getGreyMainframe().submat(Helper.cangeRectForFittingMat(getMainframe(),rotatedRect.boundingRect())));
//            System.out.println(x);
//        });

//        Point linksOben = new Point(Math.round(circles.get(0, 0)[0]), Math.round(circles.get(0, 0)[1]));


//        for (int x = 1; x < circles.cols(); x++) {
//            double[] c = circles.get(0, x);
//            //Finde Radius und Mittelpunkt
//            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
//            int radius = (int) Math.round(c[2]);
//
//            if ()
//                Imgproc.circle(mat, center, radius, new Scalar(255, 255, 255));
//            Imgproc.putText(mat, center.toString(), center, 1, 1, new Scalar(255, 255, 255));
//
//
//        }

        val circles = getCircels(getGreyMainframe());
        val sixpack = new ArrayList<Circle>();

        for (int i = 0; i < circles.size() - 1; i++) {
            for (int j = i + 1; j < circles.size(); j++) {
                if (vielleichtTeilDesSixpacks(circles.get(i), circles.get(j))) {
                    Imgproc.line(mat, circles.get(i).getCenter(), circles.get(j).getCenter(), new Scalar(255, 255, 255), 10);
                    sixpack.add(circles.get(i));
                }
            }
            Imgproc.circle(mat, circles.get(i).getCenter(), circles.get(i).getRadius(), new Scalar(255, 255, 255));
        }


//        https://stackoverflow.com/questions/40688491/opencv-getperspectivetransform-and-warpperspective-java

        //     https://github.com/badlogic/opencv-fun/blob/master/src/pool/tests/PerspectiveTransform.java

//        val corner = new ArrayList<Point>();
//        corner.add(sixpack.get(0).getCenter());
//        corner.add(sixpack.get(1).getCenter());
//        corner.add(sixpack.get(4).getCenter());
//        corner.add(sixpack.get(5).getCenter());
//
//        Mat t = Mat.zeros(new Size(512, 512), mat.type());
//        List<Point> target = new ArrayList<Point>();
//        target.add(new Point(0, 0));
//        target.add(new Point(mat.cols(), 0));
//        target.add(new Point(mat.cols(), mat.rows()));
//        target.add(new Point(0, mat.rows()));
//
//
//        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
//        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
//        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);
//        Mat invTrans = Imgproc.getPerspectiveTransform(targetMat, cornersMat);
//        Mat proj = new Mat();
//        Imgproc.warpPerspective(mat, proj, trans, new Size(mat.cols(), mat.rows()));


        HighGui.imshow("circle", mat);

        HighGui.waitKey();

    }

    protected Mat getLineDedection(Mat mat) {
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(mat, linesP, 1, Math.PI / 180, 15, 100, 20); // runs the actual detection

        return linesP;
    }


    private void drawText(Mat drawing, Point ofs, String text) {
        Imgproc.putText(drawing, text, ofs, 0, 0.5, new Scalar(255, 255, 25));
    }


    /**
     * @param mat
     * @return
     */
    private List<Circle> getCircels(Mat mat) {
        val list = new LinkedList<Circle>();
        Mat circles = new Mat();
        Imgproc.HoughCircles(mat, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double) 50, // change this value to detect circles with different distances to each other
                100, 50, 50);

        for (int x = 1; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            //Finde Radius und Mittelpunkt
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);

            list.add(new Circle(center, radius));
        }
        return list;
    }

    private boolean vielleichtTeilDesSixpacks(Circle c1, Circle c2) {
        val x = 0.1;
        val relationNachPlan = 0.30;

        val relRadius = (double) c1.getRadius() / c2.getRadius();
        //unterschiedliche größen
        if (!(relRadius + x > 1.0 && relRadius < 1.0 + x)) {
            return false;
        }

        //Proprtionen check
        val rel = ((double) c1.getRadius()) / (Helper.calculateDistanceBetweenPointsWithPoint2D(c1.getCenter(), c2.getCenter()) + c1.getRadius());
        return rel < relationNachPlan * (1 + x) && rel * (1 + x) > relationNachPlan;


    }

    private List<Point> findeEckenDesSixpacks(List<Circle> circles) {
        val clockwiseSorted = new LinkedList<Circle>();
        circles.sort((o1, o2) -> (int) (Helper.calculateDistanceBetweenPointsWithPoint2D(o2.getCenter(), new Point(0, 0)) - Helper.calculateDistanceBetweenPointsWithPoint2D(o1.getCenter(), new Point(0, 0))));
        Circle ol = circles.get(0);
        Circle or;
        Circle ur;
        Circle ul;
        Circle next = ol;





        return null;

    }

    private List<Circle> getNachbarn(Circle c ,List<Circle> circles){
        val nachbarn = new LinkedList<Circle>();
        for (int x = 0; x < circles.size(); x++) {
            if(!c.equals(circles.get(x)) && vielleichtTeilDesSixpacks(c,circles.get(x))){
                nachbarn.add(circles.get(x));
            }
        }
        return nachbarn;
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
//        new Cessna172Cockpit(Imgcodecs.imread("data/example/je.jpg"));
        new Cessna172Cockpit(Imgcodecs.imread("data/example/aerofly_fs_c172_flare.jpg"));

    }

}
