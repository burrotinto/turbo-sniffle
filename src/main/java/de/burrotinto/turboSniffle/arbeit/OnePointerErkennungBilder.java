package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.meters.gauge.GaugeOnePointer;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.meters.gauge.NotGaugeWithPointerException;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.math3.util.Precision;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OnePointerErkennungBilder implements Arbeit {


    @SneakyThrows
    @Override
    public void machDeinDing() {
//        val files = listFiles(Paths.get("data/example/gauge")).stream().filter(path -> path.toString().contains("value=0_min=0_max=15_step=5_id=1.jpg")).collect(Collectors.toList());
        val files = listFiles(Paths.get("data/example/gauge"));

        for (int i = 0; i < files.size(); i++) {
            val file = files.get(i).toString();
            val path = files.get(i);
            val name = file.split("\\\\")[file.split("\\\\").length - 1].split("\\.")[0];

            val exampleFile = new ExampleFile(name);

            val gauge = GaugeFactory.getGauge(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE));
//            Imgcodecs.imwrite("data/out/" + name + "_1_source.png", gauge.getSource());
//            Imgcodecs.imwrite("data/out/" + name + "_2_canny.png", gauge.getCanny());
//            Imgcodecs.imwrite("data/out/" + name + "_3_otsu.png", gauge.getOtsu());
            new Thread(() -> {

                try {
                    GaugeOnePointer analogOnePointer = GaugeFactory.getGaugeWithOnePointerAutoScale(gauge, exampleFile.getSteps(), exampleFile.getMin(), exampleFile.getMax());

//                Imgcodecs.imwrite("data/out/" + name + "_3_ellipse.png", maskiert);
//                Imgcodecs.imwrite("data/out/" + name + "_4_transponiert.png", transponiert);
//                Imgcodecs.imwrite("data/out/" + name + "_5_gedreht.png", gedreht);

//                Imgcodecs.imwrite("data/out/" + name + "_8_otsu.png", gauge.getSource());

//                    Imgcodecs.imwrite("data/out/" + name + "|_8_otsu.png", analogOnePointer.getOtsu());
//                    Imgcodecs.imwrite("data/out/" + name + "|_9_idealisiert.png", analogOnePointer.getIdealisierteDarstellung());
                    Imgcodecs.imwrite("data/out/" + name + "_comp=" + Precision.round(analogOnePointer.getValue(), 2) + "_10_dedected.png", analogOnePointer.getDrawing(analogOnePointer.getSource().clone()));
                } catch (NotGaugeWithPointerException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }


    // list all files from this path
    public static List<Path> listFiles(Path path) throws IOException {

        List<Path> result;
        try (Stream<Path> walk = Files.walk(path)) {
            result = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;

    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        new OnePointerErkennungBilder().machDeinDing();
    }
}