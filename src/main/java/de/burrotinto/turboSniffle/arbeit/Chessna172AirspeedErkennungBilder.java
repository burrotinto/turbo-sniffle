package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.meters.gauge.Cessna172AirspeedIndecator;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class Chessna172AirspeedErkennungBilder implements Arbeit {


    @SneakyThrows
    @Override
    public void machDeinDing() {
        val files = OnePointerErkennungBilder.listFiles(Paths.get("data/example/cessna172"));

        for (int i = 0; i < files.size(); i++) {
            val file = files.get(i).toString();
            val path = files.get(i);
            val name = file.split("\\\\")[file.split("\\\\").length - 1].split("\\.")[0];

//            val exampleFile = new ExampleFile(name);

            Mat airspeed = Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE).submat(425, 575, 425, 575);
            Cessna172AirspeedIndecator ai = GaugeFactory.getCessna172AirspeedIndecator(airspeed);
            HighGui.imshow("", ai.getDrawing(ai.getSource()));
            HighGui.waitKey();

//            System.out.println( GaugeFactory.getCessna172AirspeedIndecator(airspeed).ge);
//            val gauge = GaugeFactory.getGauge(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE));
//            Imgcodecs.imwrite("data/out/" + name + "_1_source.png", gauge.getSource());
//            Imgcodecs.imwrite("data/out/" + name + "_2_canny.png", gauge.getCanny());
//            Imgcodecs.imwrite("data/out/" + name + "_3_otsu.png", gauge.getOtsu());
//            new Thread(() -> {
//
//                try {
//                    GaugeOnePointer analogOnePointer = GaugeFactory.getGaugeWithOnePointerAutoScale(gauge, exampleFile.getSteps(), exampleFile.getMin(), exampleFile.getMax());
//
////                Imgcodecs.imwrite("data/out/" + name + "_3_ellipse.png", maskiert);
////                Imgcodecs.imwrite("data/out/" + name + "_4_transponiert.png", transponiert);
////                Imgcodecs.imwrite("data/out/" + name + "_5_gedreht.png", gedreht);
//
////                Imgcodecs.imwrite("data/out/" + name + "_8_otsu.png", gauge.getSource());
//                    Imgcodecs.imwrite("data/out/" + name + "|_8_otsu.png", analogOnePointer.getOtsu());
//                    Imgcodecs.imwrite("data/out/" + name + "|_9_idealisiert.png", analogOnePointer.getIdealisierteDarstellung());
//                    Imgcodecs.imwrite("data/out/" + name + "_comp=" + Precision.round(analogOnePointer.getValue(), 2) + "_10_dedected.png", analogOnePointer.getDrawing(analogOnePointer.getSource().clone()));
//                } catch (NotGaugeWithPointerException e) {
//                    e.printStackTrace();
//                }
//            }).start();

        }
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        new Chessna172AirspeedErkennungBilder().machDeinDing();
    }
}
