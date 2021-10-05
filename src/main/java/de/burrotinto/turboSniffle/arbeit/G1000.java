package de.burrotinto.turboSniffle.arbeit;

import de.burrotinto.turboSniffle.gauge.GarminG1000;
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


//        Mat g1000 = Imgcodecs.imread(G1000, Imgcodecs.IMREAD_GRAYSCALE);
//        new GarminG1000(g1000);


        ArrayList<Point> corner = new ArrayList<>();
        corner.add(new Point(329, 202));
        corner.add(new Point(966, 202));
        corner.add(new Point(954, 661));
        corner.add(new Point(343, 662));
        Mat g1000 = Imgcodecs.imread("data/example/g1000Auswertung/0.png", Imgcodecs.IMREAD_GRAYSCALE);
//        Mat g1000 = Imgcodecs.imread("data/example/G1000/2021-09-08 11_12_48-Window.png", Imgcodecs.IMREAD_GRAYSCALE);
        new GarminG1000(g1000);


//        ArrayList<Point> corner = new ArrayList<>();
//        corner.add(new Point(404, 191));
//        corner.add(new Point(1317, 192));
//        corner.add(new Point(1296, 843));
//        corner.add(new Point(423, 841));
//        new GarminG1000(transformieren(Imgcodecs.imread("data/example/G1000/Cessna_172SP_G1000 - 2021-09-03 14.08.20.png", Imgcodecs.IMREAD_GRAYSCALE), corner, GarminG1000.SIZE));

//
//        List<Point> corner = new ArrayList<>();
//        corner.add(new Point(880, 383));
//        corner.add(new Point(2015, 376));
//        corner.add(new Point(2023, 1257));
//        corner.add(new Point(899, 1235));
//        new GarminG1000(transformieren(Imgcodecs.imread("data/example/G1000/als-sim-al42-photo-alsim-al42-simulateur-04.jpg", Imgcodecs.IMREAD_GRAYSCALE), corner, GarminG1000.SIZE));


    }

    public static Mat transformieren(Mat mat, RotatedRect ellipse, Size size) {
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transformieren(mat, Arrays.stream(pts).collect(Collectors.toList()), size);
    }

    public static Mat transformieren(Mat mat, List<Point> corner, Size size) {

        List<Point> target = new ArrayList<Point>();
        target.add(new Point(0, 0));
        target.add(new Point(size.width, 0));
        target.add(new Point(size.width, size.height));
        target.add(new Point(0, size.height));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);

        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, size);

        return proj;
    }
}

