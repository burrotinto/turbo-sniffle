package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.meters.gauge.impl.GrowingMethod;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;


@Service
public class CLAHE implements Arbeit {
    private static final String FILE = "data/example/gauge/value=0_min=0_max=6_step=1_id=SWN.jpeg";
    private static final int BILATERAL_D = 20;

    public static void main(String[] args) {
        new CLAHE().machDeinDing();
    }

    @Override
    public void machDeinDing() {
        nu.pattern.OpenCV.loadLocally();

        Mat x = Imgcodecs.imread(FILE, Imgcodecs.IMREAD_GRAYSCALE);
        Imgcodecs.imwrite("data/out/orginal.png", x);
        Mat bilateral = new Mat();
        Imgproc.bilateralFilter(x, bilateral, BILATERAL_D, BILATERAL_D * 2.0, BILATERAL_D * 0.5);
        Imgcodecs.imwrite("data/out/BILATERAL_org.png", bilateral);


        Imgproc.createCLAHE(2.0,new Size(8,8)).apply(bilateral,bilateral);
        Imgcodecs.imwrite("data/out/CLAHE_filter.png", bilateral);

    }
}
