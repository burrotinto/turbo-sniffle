package de.burrotinto.turboSniffle.meters.gauge;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;

import java.util.List;

public interface Measuring {
    double getValue();
    void update(Mat mat);
    Mat getDrawing();
    List<MatOfPoint> getContoures();
    Size getSize();
}
