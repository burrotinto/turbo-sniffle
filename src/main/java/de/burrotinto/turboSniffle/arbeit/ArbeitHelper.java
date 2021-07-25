package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.cv.Helper;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class ArbeitHelper {

    public static Mat drawGitter(Mat src, int gitterPX){
        Mat out = src.clone();
        for (int i = 0; i < src.cols(); i+=gitterPX) {
            Imgproc.line(out,new Point(i,0),new Point(i, src.rows()), Helper.BLACK);
            Imgproc.putText(out,""+i,new Point(i,gitterPX),0,0.5,Helper.BLACK);
        }
        for (int j = 0; j < src.rows(); j+= gitterPX) {
            Imgproc.line(out,new Point(0,j),new Point(src.cols(),j), Helper.BLACK);
            Imgproc.putText(out,""+j,new Point(0,j),0,0.5,Helper.BLACK);
        }
        return out;
    }
}
