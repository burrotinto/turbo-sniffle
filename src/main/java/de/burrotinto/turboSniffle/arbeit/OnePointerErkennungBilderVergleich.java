package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.gauge.Gauge;
import de.burrotinto.turboSniffle.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.gauge.AutoEncoderGauge;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.CvType;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OnePointerErkennungBilderVergleich implements Arbeit {


    @SneakyThrows
    @Override
    public void machDeinDing() {
        try {


//            val files = listFiles(Paths.get("data/example/gauge")).stream().filter(path -> path.toString().contains("value=0_min=0_max=6_step=1_id=SWN.jpeg")).collect(Collectors.toList());
            val files = listFiles(Paths.get("data/example/gaugeDedection"));

            int maxPictures = 10;
            Mat all = new Mat(new Size(Gauge.DEFAULT_SIZE.width * 4, Gauge.DEFAULT_SIZE.height * maxPictures), CvType.CV_8UC3);


            for (int i = 0; i < files.size(); i++) {


                val file = files.get(i).toString();

                Mat orgRezice = new Mat(Gauge.DEFAULT_SIZE, Gauge.TYPE);
                Imgproc.resize(Imgcodecs.imread(file, Imgcodecs.IMREAD_ANYCOLOR), orgRezice, Gauge.DEFAULT_SIZE);
                orgRezice.copyTo(all.rowRange((int) Gauge.DEFAULT_SIZE.height * (i % maxPictures), (int) Gauge.DEFAULT_SIZE.height * ((i % maxPictures) + 1)).colRange(0, (int) Gauge.DEFAULT_SIZE.width));
                try {
                    Gauge g = GaugeFactory.getGaugeEllipseMethod(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE), 20);
                    AutoEncoderGauge gg = GaugeFactory.getGaugeWithOnePointerAutoScale(g);
                    Mat mat = gg.getDrawing(gg.getSource());
                    mat.copyTo(all.rowRange((int) Gauge.DEFAULT_SIZE.height * (i % maxPictures), (int) Gauge.DEFAULT_SIZE.height * ((i % maxPictures) + 1)).colRange((int) Gauge.DEFAULT_SIZE.width, (int) Gauge.DEFAULT_SIZE.width * 2));
                } catch (Exception e) {

                }
                try {
                    Gauge g = GaugeFactory.getGaugeWithHeatMap(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE), 20);
                    AutoEncoderGauge gg = GaugeFactory.getGaugeWithOnePointerAutoScale(g);

                    Mat mat = gg.getDrawing(gg.getSource());
                    mat.copyTo(all.rowRange((int) Gauge.DEFAULT_SIZE.height * (i % maxPictures), (int) Gauge.DEFAULT_SIZE.height * ((i % maxPictures) + 1)).colRange((int) Gauge.DEFAULT_SIZE.width * 2, (int) Gauge.DEFAULT_SIZE.width * 3));
                } catch (Exception e) {

                }
                try {
                    Gauge g = GaugeFactory.getGauge(Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE));
                    AutoEncoderGauge gg = GaugeFactory.getGaugeWithOnePointerAutoScale(g);

                    Mat mat = gg.getDrawing(gg.getSource());
                    mat.copyTo(all.rowRange((int) Gauge.DEFAULT_SIZE.height * (i % maxPictures), (int) Gauge.DEFAULT_SIZE.height * ((i % maxPictures) + 1)).colRange((int) Gauge.DEFAULT_SIZE.width * 3, (int) Gauge.DEFAULT_SIZE.width * 4));
                } catch (Exception e) {

                }
//                HighGui.imshow("aaa", all);
//                HighGui.waitKey(1);
//
                Imgcodecs.imwrite("data/out/vergleich_" + i / 10 + ".png", all);
                if (i % maxPictures == 0 && i != 0) {
                    all = new Mat(new Size(Gauge.DEFAULT_SIZE.width * 4, Gauge.DEFAULT_SIZE.height * maxPictures), CvType.CV_8UC3);
                }
                System.out.println(file);
            }


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
        new OnePointerErkennungBilderVergleich().machDeinDing();
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
