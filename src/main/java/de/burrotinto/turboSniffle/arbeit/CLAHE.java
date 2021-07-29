package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.meters.gauge.impl.GrowingMethod;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;


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

        val cannyBi = GaugeFactory.getCanny();
        cannyBi.setSourceImage((BufferedImage) HighGui.toBufferedImage(bilateral));
        cannyBi.process();
        Imgcodecs.imwrite("data/out/CANNY_nach_bilateral.png", cannyBi.getEdgeMat());


        Imgproc.createCLAHE(2.0,new Size(8,8)).apply(bilateral,bilateral);
        Imgcodecs.imwrite("data/out/CLAHE_filter.png", bilateral);

        val canny = GaugeFactory.getCanny();
        canny.setSourceImage((BufferedImage) HighGui.toBufferedImage(bilateral));
        canny.process();
        Imgcodecs.imwrite("data/out/CANNY_nach_clahe.png", canny.getEdgeMat());

        val cannyORG = GaugeFactory.getCanny();
        cannyORG.setSourceImage((BufferedImage) HighGui.toBufferedImage(Imgcodecs.imread(FILE, Imgcodecs.IMREAD_GRAYSCALE)));
        cannyORG.process();
        Imgcodecs.imwrite("data/out/CANNY_ohne_clahe.png", cannyORG.getEdgeMat());

        GaugeFactory.getGauge(Imgcodecs.imread(FILE, Imgcodecs.IMREAD_GRAYSCALE));
    }
}
