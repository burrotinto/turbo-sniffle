package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.meters.gauge.AutoEncoderGauge;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class Chessna172AirspeedErkennungBilder implements Arbeit {


    @SneakyThrows
    @Override
    public void machDeinDing() {
        val files = OnePointerErkennungBilder.listFiles(Paths.get("data/example/sixpack"));
//                .stream().filter(path -> path.toString().contains("cessna172_017")).collect(Collectors.toList());

        for (int i = 0; i < files.size(); i++) {
            val file = files.get(i).toString();
            val path = files.get(i);
            val name = file.split("\\\\")[file.split("\\\\").length - 1].split("\\.")[0];
//
//            HighGui.imshow("", ArbeitHelper.drawGitter( Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE),50));
//            HighGui.waitKey();

            Mat airspeed = Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE).submat(50, 375, 150, 475);




            val ai = GaugeFactory.getCessna172AirspeedIndecator(airspeed);
            Imgproc.putText(airspeed,""+ai.getValue(),new Point(50,50),Imgproc.FONT_HERSHEY_PLAIN,1.0, Helper.WHITE);
            HighGui.imshow("xccccc", ai.getDrawing(ai.getSource().clone()));
//            HighGui.imshow("xccccc", airspeed);
            HighGui.waitKey(100);



        }
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        new Chessna172AirspeedErkennungBilder().machDeinDing();
    }
}
