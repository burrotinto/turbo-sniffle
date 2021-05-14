package de.burrotinto.popeye.meters;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class AnalogOneArrow {
    private Mat src, cannyOutput;
    private int threshold = 130;
    private RotatedRect minEllipseDisplay;

    private Pointer pointer;

    public AnalogOneArrow(Mat src) {
        this.src = src.clone();
        cannyOutput = new Mat();

        Imgproc.Canny(src, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        RotatedRect[] minEllipse = new RotatedRect[contours.size()];
        int indexMax =0;

        for (int i = 0; i < contours.size(); i++) {
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5) {
                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));
            }
            if(minEllipse[i].size.area() > minEllipse[indexMax].size.area()){
                indexMax = i;
            }
        }
        minEllipseDisplay =  minEllipse[indexMax];

        Imgproc.pointPolygonTest(new MatOfPoint2f(contours.get(indexMax).toArray()),new Point(0,0),false);


    }

    public RotatedRect getDisplay(){
        return minEllipseDisplay;
    }

    public Point getCenter(){
       return minEllipseDisplay.center;
    }
}
