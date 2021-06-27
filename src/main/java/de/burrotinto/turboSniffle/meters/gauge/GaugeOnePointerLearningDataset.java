package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.booleanAutoEncoder.BooleanAutoencoder;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GaugeOnePointerLearningDataset {
    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    @SneakyThrows
    public static Mat AUFROLLEN(Mat m, double steps) {
        Mat out = new Mat(new Size(steps, Math.min(m.size().height, m.size().width) / 2), m.type());
        int nextcol = 0;
        for (double i = 0; i < 360; i += 360 / steps) {
            Mat rMap = new Mat();
            val rotate = Imgproc.getRotationMatrix2D(new Point(m.size().width / 2, m.size().height / 2), i, 1.0);
            Imgproc.warpAffine(m, rMap, rotate, m.size());

            for (int j = 0; j < m.size().width / 2; j++) {
                double[] x = rMap.get(j+(int) rMap.size().height / 2,(int) rMap.size().height / 2);
                System.out.println(x[0]);
                out.put((int) rMap.size().height / 2-j, nextcol, x);
            }
            nextcol++;

        }

        HighGui.imshow("as",out);
        return out;

    }

    public static List<GaugeWithOnePointer> getTrainingset(Size size, double angleSteps) {
        Mat white = Mat.zeros(size, CvType.CV_8UC3);
        Imgproc.circle(white, new Point(size.width / 2, size.height / 2), (int) size.width / 2, WHITE, -1);
        Imgproc.line(white, new Point(size.width / 2, size.height / 2), new Point(size.width, size.height / 2), BLACK, Math.max((int) size.height / 32, 2));


        Mat black = Mat.ones(size, CvType.CV_8UC3);
        Imgproc.circle(black, new Point(size.width / 2, size.height / 2), (int) size.width / 2, BLACK, -1);
        Imgproc.line(black, new Point(size.width / 2, size.height / 2), new Point(size.width, size.height / 2), WHITE, Math.max((int) size.height / 32, 2));


        ArrayList<GaugeWithOnePointer> list = new ArrayList<>();
        for (double i = 0; i < 360; i += angleSteps) {
            Mat dstW = new Mat();
            Mat dstB = new Mat();
            val rotate = Imgproc.getRotationMatrix2D(new Point(size.width / 2, size.height / 2), i, 1.0);
            Imgproc.warpAffine(white, dstW, rotate, size);
            Imgproc.warpAffine(black, dstB, rotate, size);

            list.add(new GaugeWithOnePointer(dstW, i));
            list.add(new GaugeWithOnePointer(dstB, i));
        }

        return list;
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();

        val size = new Size(512, 512);
        val train = GaugeOnePointerLearningDataset.getTrainingset(size, 2);

        GaugeWithOnePointer g = new GaugeWithOnePointer(GaugeExtraction.extract(Imgcodecs.imread("data/example/temp.jpg")).getSource());
//        GaugeWithOnePointer g = new GaugeWithOnePointer(GaugeExtraction.extract(Imgcodecs.imread("data/example/Li_Example_1.png")).getSource());

        HighGui.imshow("", AUFROLLEN(g.getSource(), 720));
        HighGui.waitKey();
        val gauge = g.toSize(size);

        int tres = 100;

        long min = Long.MAX_VALUE;
        int iMin = 0;
        for (int i = 0; i < train.size(); i++) {
            val dist = BooleanAutoencoder.DISTANZ(train.get(i).getSource(), gauge, tres, min);
            if (dist < min) {
                min = dist;
                iMin = i;
            }
        }


        val d = Mat.zeros(size, train.get(iMin).getSource().type());
        for (int i = 0; i < d.rows(); i++) {
            for (int j = 0; j < d.cols(); j++) {
                d.put(i, j, Math.abs(gauge.get(i, j)[0] - train.get(iMin).getSource().get(i, j)[0]) > tres ? 255 : 0);

            }
        }

        val d2 = Mat.zeros(size, train.get(iMin).getSource().type());
        for (int i = 0; i < d.rows(); i++) {
            for (int j = 0; j < d.cols(); j++) {
                d2.put(i, j, Math.abs(gauge.get(i, j)[0] - train.get(90).getSource().get(i, j)[0]) > tres ? 255 : 0);

            }
        }
        System.out.println(BooleanAutoencoder.DISTANZ(train.get(iMin).getSource(), gauge, tres, Long.MAX_VALUE) + " " + BooleanAutoencoder.DISTANZ(train.get(90).getSource(), gauge, tres, Long.MAX_VALUE));
        HighGui.imshow("d", d);
        HighGui.imshow("d2", d2);

        System.out.println(train.get(iMin).getPointerAngel() + " ");
        g.setPointerAngel(train.get(iMin).getPointerAngel());
        Imgproc.line(g.getSource(), g.getPointer().p1, g.getPointer().p2, new Scalar(0, 0, 0), 2);
        HighGui.imshow("eq", train.get(iMin).getSource());
        HighGui.imshow("ga", g.getSource());
        HighGui.waitKey();
    }
}


