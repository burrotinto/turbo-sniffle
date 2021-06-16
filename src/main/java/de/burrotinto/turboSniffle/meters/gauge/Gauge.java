package de.burrotinto.turboSniffle.meters.gauge;

import lombok.val;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Gauge {
    public Gauge(Mat input) {
        val greyInput = Mat.zeros(input.size(), input.type());
        Imgproc.cvtColor(input, greyInput, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.resize(greyInput,greyInput,new Size(256,256));

        val canny = getEdgeDedectionCanny(greyInput,85);
        val sobel = getSobel(greyInput,85);
        HighGui.imshow("CANNY",canny);
        HighGui.imshow("SOBEL",sobel);



        Mat out = new Mat(sobel.size(),sobel.type());
        val sobelLines = getLineDedection(sobel);
        val cannyLines = getLineDedection(canny);

        for (int x = 0; x < sobelLines.rows(); x++) {
            double[] l = sobelLines.get(x, 0);
            Imgproc.line(out, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255, 255, 255), 3, Imgproc.LINE_8, 0);
        }

//        HighGui.imshow("SOBEL_LINES",out);
//        HighGui.waitKey();


        Imgproc.ellipse(canny, getGreatestElipse(removeLines(canny,cannyLines)),new Scalar(255,255,255),10);
        Imgproc.ellipse(sobel, getGreatestElipse(removeLines(sobel,sobelLines)),new Scalar(255,255,255),10);
        HighGui.imshow("CANNY_e",canny);
        HighGui.imshow("SOBEL_e",sobel);

        HighGui.imshow("trans Sobel",transponiere(input,getGreatestElipse(removeLines(sobel,sobelLines))));
        HighGui.imshow("trans Canny",transponiere(input,getGreatestElipse(removeLines(canny,cannyLines))));
//                HighGui.imshow("trans Sobel",transponiere(input,getGreatestElipse(sobel)));
//        HighGui.imshow("trans Canny",transponiere(input,getGreatestElipse(canny)));

        HighGui.waitKey();
    }







   public static Mat getSobel(Mat input,int threshold){
        // First we declare the variables we are going to use
        Mat grad = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();

        Imgproc.Sobel( input, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT );
        Imgproc.Sobel( input, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_DEFAULT );

        // converting back to CV_8U
        Core.convertScaleAbs( grad_x, abs_grad_x );
        Core.convertScaleAbs( grad_y, abs_grad_y );
        Core.addWeighted( abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad );

        val out = new Mat(grad.size(),grad.type());
        for (int i = 0; i < grad.rows(); i++) {
            for (int j = 0; j < grad.cols(); j++) {
                if(grad.get(i,j)[0]> threshold){
                    out.put(i,j,255.0,255.0,255.0);
                } else {
                    out.put(i,j,0,0,0);
                }

            }
        }
        return out;
    }

    public static Mat getEdgeDedectionCanny(Mat mat, int threshold){
        //Canny and Contours finding
        Mat cannyOutput = new Mat();
        Imgproc.Canny(mat, cannyOutput, threshold, threshold * 2);
        return cannyOutput;
    }

   public static Mat getLineDedection(Mat mat) {
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(mat, linesP, 1, Math.PI / 180, 255 / 4, mat.size().height/4.0, mat.size().height/25.5); // runs the actual detection

        return linesP;
    }


    public static RotatedRect getGreatestElipse(Mat edgeDetected) {
        val contours = new ArrayList<MatOfPoint>();
        var hierarchy = new Mat();
        Imgproc.findContours(edgeDetected, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Check und initialisierung der AnzeigeDabei wer
        RotatedRect[] minEllipse = new RotatedRect[contours.size()];

        int indexDisplay = 0;



        val maxQuadrat = new Rect(0,0,edgeDetected.width(),edgeDetected.height());

        for (int i = 0; i < contours.size(); i++) {
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5) {
                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
            }


            val radius = (int) Math.max(minEllipse[i].size.width,minEllipse[i].size.height)/2;

            if (minEllipse[i].size.area() > minEllipse[indexDisplay].size.area()
                    && minEllipse[i].center.x - radius >= 0
                    && minEllipse[i].center.y - radius >= 0
                    && minEllipse[i].center.x + radius < edgeDetected.width()
                    && minEllipse[i].center.y + radius < edgeDetected.height()
            ) {
                indexDisplay = i;
            }
        }

        return minEllipse[indexDisplay];
    }


    static public Mat removeLines(Mat mat, Mat lines) {
        // Draw the lines
        Mat out = mat.clone();
        for (int x = 0; x < lines.rows(); x++) {
            double[] l = lines.get(x, 0);
            Imgproc.line(out, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 5, Imgproc.LINE_8, 0);
        }
        return out;
    }

    public static Mat transponiere(Mat mat, RotatedRect ellipse){
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transponiere(mat, Arrays.stream(pts).collect(Collectors.toList()));
    }
    public static Mat transponiere(Mat mat, List<Point> corner){
        //        https://stackoverflow.com/questions/40688491/opencv-getperspectivetransform-and-warpperspective-java

        //     https://github.com/badlogic/opencv-fun/blob/master/src/pool/tests/PerspectiveTransform.java



        List<Point> target = new ArrayList<Point>();
        target.add(new Point(0, 0));
        target.add(new Point(mat.cols(), 0));
        target.add(new Point(mat.cols(), mat.rows()));
        target.add(new Point(0, mat.rows()));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);
        Mat invTrans = Imgproc.getPerspectiveTransform(targetMat, cornersMat);
        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, new Size(mat.cols(), mat.rows()));
        Imgproc.resize(proj,proj,new Size(512,512));
        return proj;
    }
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        new Gauge(Imgcodecs.imread("data/example/li.png"));
    }
}
