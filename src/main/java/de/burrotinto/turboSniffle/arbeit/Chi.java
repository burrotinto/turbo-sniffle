package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.meters.gauge.impl.GrowingMethod;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;


@Service
public class Chi implements Arbeit {
    public static void main(String[] args) {
        new Chi().machDeinDing();
    }

    @Override
    public void machDeinDing() {
        nu.pattern.OpenCV.loadLocally();

        Mat bild = Imgcodecs.imread("data/example/testTemp1.jpg", Imgcodecs.IMREAD_GRAYSCALE);
        Point hp = GrowingMethod.getHighestGray(bild);

        for (int i = 0; i < 255 ; i+=5) {
            Mat zero = Mat.zeros(bild.size(), bild.type());
            GrowingMethod.getGrowingMethodIterativ(bild, zero, hp, i);

            Imgcodecs.imwrite("data/growing_" + i + "Kontrast.jpg", zero);
        }





    }
}
