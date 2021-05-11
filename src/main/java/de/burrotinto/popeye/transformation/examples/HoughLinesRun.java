package de.burrotinto.popeye.transformation.examples;

import de.burrotinto.popeye.transformation.CircleExtractor;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class HoughLinesRun {
    public void run(String filename) {
        // Declare the output variables
        new CircleExtractor().getAllCircles(Imgcodecs.imread(filename)).forEach(src -> {
            Mat dst = new Mat(), cdst = new Mat(), cdstP;
            // Load an image
            // Edge detection
            Imgproc.Canny(src, dst, 50, 200, 3, false);
            // Copy edges to the images that will display the results in BGR
            Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
            cdstP = cdst.clone();
            // Standard Hough Line Transform
            Mat lines = new Mat(); // will hold the results of the detection
            Imgproc.HoughLines(dst, lines, 1, Math.PI / 180, 150); // runs the actual detection
            // Draw the lines
            for (int x = 0; x < lines.rows(); x++) {
                double rho = lines.get(x, 0)[0],
                        theta = lines.get(x, 0)[1];
                double a = Math.cos(theta), b = Math.sin(theta);
                double x0 = a * rho, y0 = b * rho;
                Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
                Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
                Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
            }
            // Probabilistic Line Transform
            Mat linesP = new Mat(); // will hold the results of the detection
            Imgproc.HoughLinesP(dst, linesP, 1, Math.PI / 180, 50, 80, 10); // runs the actual detection
            // Draw the lines
            for (int x = 0; x < linesP.rows(); x++) {
                double[] l = linesP.get(x, 0);
                Imgproc.line(cdstP, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
            }
            // Show results
            HighGui.imshow("Source " + src.toString(), src);
            HighGui.imshow("Detected Lines (in red) - Standard Hough Line Transform " + src.toString(), cdst);
            HighGui.imshow("Detected Lines (in red) - Probabilistic Line Transform " + src.toString(), cdstP);
            // Wait and Exit
        });
        HighGui.waitKey();
        System.exit(0);
    }

    public static void main(String[] args) {
        // Load the native library.
        nu.pattern.OpenCV.loadLocally();
//        new HoughLinesRun().run("pic/analog/unnamed.jpg");

        new HoughLinesRun().run("sixpacks/sixpack.jpg");
    }
}
