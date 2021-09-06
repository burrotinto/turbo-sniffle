package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.meters.gauge.GarminG1000;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class G1000 implements Arbeit {
    private static final String G1000 = "data/ae/G1000/GarminG1000.png";
    private static final String TEST = "data/ae/G1000/als-sim-al42-photo-alsim-al42-simulateur-04.jpg";
    private static final int BILATERAL_D = 20;

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

        new G1000().machDeinDing();
    }

    @Override
    public void machDeinDing() {
        Mat g1000 = Imgcodecs.imread(G1000, Imgcodecs.IMREAD_GRAYSCALE);
        new GarminG1000(g1000);
    }
}

