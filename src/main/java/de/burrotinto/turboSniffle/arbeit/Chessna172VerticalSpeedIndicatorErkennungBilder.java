package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.gauge.Cessna172SixpackFactory;
import de.burrotinto.turboSniffle.gauge.AutoEncoderGauge;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class Chessna172VerticalSpeedIndicatorErkennungBilder implements Arbeit {


    @SneakyThrows
    @Override
    public void machDeinDing() {
        val files = OnePointerErkennungBilder.listFiles(Paths.get("data/example/sixpack"));
//                .stream().filter(path -> path.toString().contains("cessna172_01701.png")).collect(Collectors.toList());

        for (int i = 0; i < files.size(); i++) {
            val file = files.get(i).toString();
            val path = files.get(i);
            val name = file.split("\\\\")[file.split("\\\\").length - 1].split("\\.")[0];


            Mat verticalSpeed = Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE).submat(350, 675, 750, 1075);




            AutoEncoderGauge ai = Cessna172SixpackFactory.getCessna172VerticalSpeedIndicator(verticalSpeed);
//            Imgproc.putText(verticalSpeed,""+Perceai.getValue(),new Point(50,50),Imgproc.FONT_HERSHEY_PLAIN,1.0, Helper.WHITE);
            HighGui.imshow("xccccc", ai.getDrawing(ai.getSource().clone()));
//            HighGui.imshow("xccccc", airspeed);
            HighGui.waitKey(1000);



        }
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        new Chessna172VerticalSpeedIndicatorErkennungBilder().machDeinDing();
    }
}
