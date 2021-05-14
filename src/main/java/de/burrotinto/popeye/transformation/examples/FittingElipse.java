package de.burrotinto.popeye.transformation.examples;

import de.burrotinto.popeye.meters.AnalogOneArrow;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;

class FittingElipse extends AbstractSwing {


    public FittingElipse(Mat src) {
        super(src);
    }

    public FittingElipse(String file) {
        super(file);
    }

    @Override
    protected Mat getDrawing() {
        AnalogOneArrow analog = new AnalogOneArrow(srcGray);
        Mat drawing = Mat.zeros(srcGray.size(), CvType.CV_8UC3);


        Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
        // contour
//            Imgproc.drawContours(drawing, contours, i, color);
//            // ellipse
        Imgproc.ellipse(drawing, analog.getDisplay(), color, 2);
        // rotated rectangle
        Imgproc.drawMarker(drawing, analog.getCenter(), color);

        return drawing;
    }

    public static void main(String[] args) {
        // Load the native OpenCV library
        nu.pattern.OpenCV.loadLocally();
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FittingElipse("sixpacks/robinDR400_KMH.JPG");
//                new CircleExtractor().getAllCircles(Imgcodecs.imread("sixpacks/robindr400.JPG")).forEach(FindContours2::new);

            }
        });
    }
}
