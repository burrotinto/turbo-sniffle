package de.burrotinto.turboSniffle.meters.gauge.impl;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class GrowingMethod {
    private static double[] WHITE = {255.0};
    private Mat gauge;

    public static void getGrowingMethod(Mat src, Mat dst, Point point, int threshold) {
        double[] color = src.get((int) point.y, (int) point.x);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = (int) (point.x + i);
                int y = (int) (point.y + j);
                if (x >= 0 && x < src.cols() && y >= 0 && y < src.rows() && (x != 0 || y != 0)) {
                    Point next = new Point(x, y);
                    if (src.get(y, x)[0] >= color[0] - threshold && !Arrays.equals(dst.get(y, x), WHITE)) {
                        dst.put(y, x, WHITE);
                        HighGui.imshow("", dst);
                        HighGui.waitKey(1);
                        getGrowingMethod(src, dst, next, threshold);
                    }
                }
            }
        }
    }

    public static void getGrowingMethodIterativ(Mat src, Mat dst, Point point, int threshold) {
        Stack<Point> points = new Stack<>();
        points.push(point);
        double[] color = src.get((int) point.y, (int) point.x);
        while (!points.empty()) {
            Point next = points.pop();
//            double[] color = src.get((int) next.y, (int) next.x);
            dst.put((int) next.y, (int) next.x, WHITE);
//            HighGui.imshow("", dst);
//            HighGui.waitKey(1);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int x = (int) (next.x + i);
                    int y = (int) (next.y + j);

                    if (x >= 0 && x < src.cols() && y >= 0 && y < src.rows() && (x != 0 || y != 0)) {
                        Pixel n = new Pixel(new Point(x, y), (int) src.get(y, x)[0]);
                        if (src.get(y, x)[0] >= color[0] - threshold && !Arrays.equals(dst.get(y, x), WHITE)) {
                            points.push(new Point(x, y));
                        }
                    }
                }
            }
        }

    }

    public static Point getHighestGray(Mat mat) {
        Point max = new Point(0, 0);
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                if (mat.get((int) max.y, (int) max.x)[0] < mat.get(i, j)[0]) {
                    max = new Point(j, i);
                }
            }
        }
        return max;
    }

    public static List<Point> getPoints(Mat src) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < src.cols(); i++) {
            for (int j = 0; j < src.rows(); j++) {
                if (src.get(j, i)[0] != 255) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
    }
}
