package de.burrotinto.popeye.transformation.examples;

import java.util.ArrayList;
import java.util.List;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.popeye.transformation.Pair;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;

import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

class FindContours extends AbstractSwing {

    public FindContours(Mat src) {
        super(src);
    }

    public FindContours(String file) {
        super(file);
    }

    @Override
    protected Mat getDrawing() {
        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);


        RotatedRect[] minRect = new RotatedRect[contours.size()];
        List<Moments> mu = new ArrayList<>(contours.size());

        for (int i = 0; i < contours.size(); i++) {
            minRect[i] = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            mu.add(Imgproc.moments(contours.get(i)));
        }

        List<Point> mc = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            //add 1e-5 to avoid division by zero
            mc.add(new Point(mu.get(i).m10 / (mu.get(i).m00 + 1e-5), mu.get(i).m01 / (mu.get(i).m00 + 1e-5)));
        }
        Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
//        Mat drawing = srcGray.clone();

        List<MatOfPoint> hullList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }

        // getMaxScaleObject
        int maxSO = 0;
        double maxScale = 0;
        for (int i = 0; i < contours.size(); i++) {
            //Länge und breite des Objectes
            double l = Math.max(minRect[i].size.height, minRect[i].size.width);
            double b = Math.min(minRect[i].size.height, minRect[i].size.width);
            if (l / b > maxScale
                    && Double.isFinite(l / b)
                    && !Double.isNaN(l / b)
                    && cannyOutput.size().height * 0.2 < l) {
                maxScale = l / b;
                maxSO = i;
            }

        }

        for (int i = 0; i < contours.size(); i++) {
//            if (i== maxSO) {
            if (Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) > 100) {

                double l = Math.max(minRect[i].size.height, minRect[i].size.width);
                double b = Math.min(minRect[i].size.height, minRect[i].size.width);
                double scale = l / b;

                Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));

                Imgproc.drawContours(drawing, hullList, i, color);
                Pair<Point,Point> max = Helper.maxDistance(contours.get(i).toList());
//                Imgproc.line(drawing,Helper.pointAtX(max.p1,max.p2,0),max.p2,color);
                Imgproc.putText(drawing, "[" + i + "|" + scale + "]", hullList.get(i).toArray()[0], 2, 0.5, color);

                // rotated rectangle
                Point[] rectPoints = new Point[4];
                minRect[i].points(rectPoints);
                for (int j = 0; j < 4; j++) {
                    Imgproc.line(drawing, rectPoints[j], rectPoints[(j + 1) % 4], color);
                }
            }
        }
        return drawing;
    }

    public static void main(String[] args) {
        // Load the native OpenCV library
        nu.pattern.OpenCV.loadLocally();
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new FindContours("pic/analog/analogeanzeigederkraftstoffmenge.jpg");
                new FindContours("C:\\Users\\fklinger\\nxbt\\studium\\Abschlussarbeit\\turbo-sniffle3\\data\\example\\20210514_061025.jpg");
            }
        });
    }
}
