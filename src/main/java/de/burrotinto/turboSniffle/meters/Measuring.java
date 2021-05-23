package de.burrotinto.turboSniffle.meters;

import org.opencv.core.Mat;

public interface Measuring {
    double getValue();
    void update(Mat mat);
    Mat getDrawing();
}
