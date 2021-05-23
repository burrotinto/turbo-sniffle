package de.burrotinto.turboSniffle.meters;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.List;

public interface Measuring {
    double getValue();
    void update(Mat mat);
    Mat getDrawing();
    List<MatOfPoint> getContoures();
}
