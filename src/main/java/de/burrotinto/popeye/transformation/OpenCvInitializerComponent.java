package de.burrotinto.popeye.transformation;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class OpenCvInitializerComponent {
    private static final Logger logger = LoggerFactory.getLogger(OpenCvInitializerComponent.class);

    static {
        nu.pattern.OpenCV.loadLocally();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @PostConstruct
    private void init() {
        logger.info("Welcome to OpenCV " + Core.VERSION);
        Mat img = Imgcodecs.imread("F:\\Florian\\studium\\Abschlussarbeit\\popeye\\sixpack.jpg");

        String default_file = "F:\\Florian\\studium\\Abschlussarbeit\\popeye\\sixpack.jpg";
        String filename = "sixpack.jpg";

        // Load an image
        Mat src = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
        Mat src2 = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
        // Check if image is loaded fine
        if (src.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default "
                    + default_file + "] \n");
            System.exit(-1);
        }
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(gray, gray, 5);
        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double) gray.rows() / 16, // change this value to detect circles with different distances to each other
                100.0, 30.0, 60, 100); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(src, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(src, center, radius, new Scalar(255, 0, 255), 3, 8, 0);

            Mat newImage = src2.submat((int) Math.round(center.y - radius), (int) Math.round(center.y + radius), (int) Math.round(center.x - radius), (int) Math.round(center.x + radius));
            Mat resizeimage = new Mat();
            Size sz = new Size(200,200);
            Imgproc.resize( newImage, resizeimage, sz );


            Imgproc.cvtColor(resizeimage, resizeimage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.blur(resizeimage, resizeimage, new Size(5, 5));

            Imgproc.Canny(resizeimage, resizeimage, 10, 90, 3, true);

            HighGui.imshow("detected circles " + x, resizeimage);
        }
        HighGui.imshow("detected circles", src);
        HighGui.waitKey();
        System.exit(0);

        Imgcodecs.imwrite("F:\\Florian\\studium\\Abschlussarbeit\\popeye\\sixpack2.jpg", img);
    }
}