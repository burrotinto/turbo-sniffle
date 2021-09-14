package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.cv.TextDedection;
import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.AutoEncoderGauge;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.meters.gauge.NotGaugeWithPointerException;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OnePointerErkennungBilder implements Arbeit {


    @SneakyThrows
    @Override
    public void machDeinDing() {
        try {
//            String prefix = "ELLIPSE";
//            String prefix = "HEATMAP";
            String prefix = "KOMBO";

//            val files = listFiles(Paths.get("data/example/gauge")).stream().filter(path -> path.toString().contains("Airspeed")).collect(Collectors.toList());
            val files = listFiles(Paths.get("data/example/gauge"));

            val td = new TextDedection();

            Mat all = new Mat(new Size(Gauge.DEFAULT_SIZE.width * 2, Gauge.DEFAULT_SIZE.height * files.size()), Gauge.TYPE);
            for (int i = 0; i < files.size(); i++) {
                val file = files.get(i).toString();
                val path = files.get(i);
                val name = file.split("\\\\")[file.split("\\\\").length - 1].split("\\.")[0];

                val exampleFile = new ExampleFile(name);


                Gauge gauge = null;
                switch (prefix.toLowerCase(Locale.ROOT)){
                    case "heatmap": {
                        gauge = GaugeFactory.getGaugeWithHeatMap(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE), 20);
                        break;
                    }
                    case "ellipse": {
                        gauge =  GaugeFactory.getGaugeEllipseMethod(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE),20);
                        break;
                    }
                    default:{
                        gauge = GaugeFactory.getGauge(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE));
                    }
                }

                td.doOCRNumbers(gauge.getSource());

                Mat orgRezice = new Mat(Gauge.DEFAULT_SIZE, Gauge.TYPE);
                Imgproc.resize(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE), orgRezice, Gauge.DEFAULT_SIZE);
                gauge.getSource().copyTo(all.rowRange((int) Gauge.DEFAULT_SIZE.height * i, (int) Gauge.DEFAULT_SIZE.height * (i + 1)).colRange(0, (int) Gauge.DEFAULT_SIZE.width));
                gauge.getCanny().copyTo(all.rowRange((int) Gauge.DEFAULT_SIZE.height * i, (int) Gauge.DEFAULT_SIZE.height * (i + 1)).colRange((int) Gauge.DEFAULT_SIZE.width, (int) Gauge.DEFAULT_SIZE.width*2));

                Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "_1_source.png", gauge.getSource());
                Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "_2_canny.png", gauge.getCanny());
                Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "_3_otsu.png", gauge.getOtsu());

                if (prefix.toLowerCase(Locale.ROOT).contains("heatmap")) {
                    Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "_4_heatmap.png", gauge.getHeatMap().getHeadMatSkaliert());
                    Mat sum = new Mat();
                    Core.add(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE), gauge.getHeatMap().getHeadMatSkaliert(), sum);
                    Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "_5_heatmapSUM.png", sum);
                }


                Gauge finalGauge = gauge;

                    try {
                        val analogOnePointer = GaugeFactory.getGaugeWithOnePointerAutoScale(finalGauge, exampleFile.getSteps(), exampleFile.getMin(), exampleFile.getMax());
                        Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "|_8_otsu.png", analogOnePointer.getOtsu());
                        Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "|_9_idealisiert.png", analogOnePointer.getIdealisierteDarstellung());
                        Imgcodecs.imwrite("data/out/" + prefix + "_" + name + "_comp=" + Precision.round(analogOnePointer.getValue(), 2) + "_10_dedected.png", analogOnePointer.getDrawing(analogOnePointer.getSource().clone()));


                    } catch (NotGaugeWithPointerException e) {
                        e.printStackTrace();
                    }

            }

//            HighGui.imshow("aaa", all);
//            HighGui.waitKey();
        } catch (Exception e) {
            e.printStackTrace();
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

//    Mat draw = new Mat(bilateral.size(), CvType.CV_8UC3);
//                Imgproc.cvtColor(bilateral, bilateral, Imgproc.COLOR_GRAY2RGB);
//                        Imgproc.drawMarker(bilateral, heatMap.getCenter(), new Scalar(0, 0, 255), Imgproc.MARKER_CROSS, (int) dist * 2);
//                        Imgproc.circle(bilateral, heatMap.getCenter(), (int) dist, new Scalar(0, 0, 255), 10);
//
//                        Mat finalBilateral = bilateral;
//                        cluster.forEach(rotatedRect -> Helper.drawRotatedRectangle(finalBilateral, rotatedRect, new Scalar(127, 0, 0), 8));
//                        heatMap.getAllConnectedWithCenter().forEach(rotatedRect -> Helper.drawRotatedRectangle(finalBilateral, rotatedRect, new Scalar(127, 127, 127), 2));
//                        Imgcodecs.imwrite("data/out/heatmap_Color.png", bilateral);
//                        HighGui.imshow("", bilateral
//                        );
//                        HighGui.waitKey();
