package de.burrotinto.turboSniffle.meters.gauge;


import de.burrotinto.ellipse.CannyEdgeDetector;
import de.burrotinto.ellipse.EllipseDetector;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.*;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GaugeExtraction {
    public static final Scalar WHITE = new Scalar(255.0, 255.0, 255.0);

    public GaugeExtraction(Mat input) {
        System.out.println(getEdgeDedectionCanny(input, 85).type());
        val cannyEdgeDetector = getCanny();

        //1. Canny Edge Dedection mit auto Threashold
        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(input));
        cannyEdgeDetector.process();


        //2. Finden der größten Ellipse mit einem Ellipse Score über 0.8
        val biggestEllipse = getGreatestEllipseII(cannyEdgeDetector);

        //3. Alles ausserhalb der Ellipse Entfernen
        val maskiert = removeAllOutsideEllpipse(input, biggestEllipse);


        //4. Gefundene Ellipse aus Bild transponieren damit ellipse im Mittelpunkt und als Kreis dargestellt wird
        val transponiert = transponiere(maskiert, biggestEllipse);


        //5. Durch Transponieren wird das Messgerät eventuell gedreht. Hier wird das korrigiert.
        val rotate = Imgproc.getRotationMatrix2D(new Point(transponiert.width() / 2.0, transponiert.height() / 2.0), 90 - biggestEllipse.angle, 1.0);
        val gedreht = Mat.zeros(transponiert.size(), transponiert.type());
        Imgproc.warpAffine(transponiert, gedreht, rotate, transponiert.size());


        //6.
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(getEdgeDedectionCanny(gedreht, 85), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        val draw = Mat.zeros(gedreht.size(), gedreht.type());

        contours.stream().forEach(matOfPoint -> {
            val minRect = Imgproc.minAreaRect(new MatOfPoint2f(matOfPoint.toArray()));
            val p = new Mat();
            Point[] rectPoints = new Point[4];
            minRect.points(rectPoints);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(draw, rectPoints[j], rectPoints[(j + 1) % 4], WHITE);
            }
        });

        var candidaten = contours.stream().sorted((o1, o2) -> (int) (Imgproc.minAreaRect(new MatOfPoint2f(o2.toArray())).size.area() - Imgproc.minAreaRect(new MatOfPoint2f(o1.toArray())).size.area())).collect(Collectors.toList());

        candidaten = candidaten.subList(0, 3);
        Imgproc.drawContours(draw, candidaten, -1, WHITE, 5);

        HighGui.imshow("0. Input", input);
        HighGui.imshow("1. Canny", cannyEdgeDetector.getEdgeMat());
        HighGui.imshow("2 + 3. maskiert", maskiert);
        HighGui.imshow("4. transponiert", transponiert);
        HighGui.imshow("5. Gedreht", gedreht);
        HighGui.imshow("6. blackAndWhite", draw);

        HighGui.waitKey();
    }


    public static CannyEdgeDetector getCanny() {
        CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector();
//        cannyEdgeDetector.setLowThreshold(2.5f); // 2.5
//        cannyEdgeDetector.setHighThreshold(7.5f); // 7.5
        cannyEdgeDetector.setAutoThreshold(true);
        // 2 is default for CannyEdgeDetector but 1 is setting from Ellipse reference code
        cannyEdgeDetector.setGaussianKernelRadius(1f); // 2
        // 16 is default for Canny Edge Detector, but 5 is setting from ellipse reference code.
        cannyEdgeDetector.setGaussianKernelWidth(5); // 16

        return cannyEdgeDetector;
    }

    @SneakyThrows
    public RotatedRect getGreatestEllipseII(CannyEdgeDetector edgeDetector) {

        EllipseDetector ellipseDetector = new EllipseDetector();
        ellipseDetector.setEdgeImage(edgeDetector);
        ellipseDetector.setUseMedianCenter(true);
        ellipseDetector.setDistanceToEllipseContour(0.1f);
        ellipseDetector.process();


//        ImageIO.write(ellipseDetector.createFinalEllipseImage(), "png", new File("x.png"));
//

        val mat = bufferedImageToMat(edgeDetector.getEdgeImage());

        val ellipsInside = ellipseDetector.getFinalEllipseList().stream().filter(ellipse -> {
                    val e = EllipseDetector.createContour(ellipse);

                    val radius = (int) Math.max(e.size.width, e.size.height) / 2;

                    if (
                            radius > 128 &&
                            e.center.x - radius >= 0
                                    && e.center.y - radius >= 0
                                    && e.center.x + radius < mat.width()
                                    && e.center.y + radius < mat.height()
                    ) {
                        return true;
                    } else {
                        return false;
                    }
                }

        ).sorted((o1, o2) -> (o2.ellipseScore > o1.ellipseScore) ? 1 : 0);

        return EllipseDetector.createContour(ellipsInside.findFirst().get());
//        final RotatedRect[] max = {null};
//        ellipseDetector.getFinalEllipseList().forEach(ellipse -> {
//            val e = EllipseDetector.createContour(ellipse);
//            val radius = (int) Math.max(e.size.width, e.size.height) / 2;
//
//            if (max[0] == null || (
//                    ellipse.ellipseScore > 0.5 &&
//                            e.size.area() > max[0].size.area()
//                            && e.center.x - radius >= 0
//                            && e.center.y - radius >= 0
//                            && e.center.x + radius < mat.width()
//                            && e.center.y + radius < mat.height()
//            )) {
//                max[0] = e;
//            }
//        });
//        return max[0];
    }

    public RotatedRect getGreatestEllipseCanny(Mat input) {
        val cannyEdgeDetector = getCanny();

        cannyEdgeDetector.setEdgeImage((BufferedImage) HighGui.toBufferedImage(input));
        cannyEdgeDetector.process();
        return getGreatestEllipseII(cannyEdgeDetector);
    }

    public RotatedRect getGreatestEllipseII(Mat input) {
        val cannyEdgeDetector = getCanny();

        cannyEdgeDetector.setSourceImage((BufferedImage) HighGui.toBufferedImage(input));
        cannyEdgeDetector.process();
        return getGreatestEllipseII(cannyEdgeDetector);
    }


    public static Mat getEdgeDedectionSobel(Mat input, int threshold) {
        // First we declare the variables we are going to use
        Mat grad = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();

        Imgproc.Sobel(input, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT);
        Imgproc.Sobel(input, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_DEFAULT);

        // converting back to CV_8U
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);

        val out = new Mat(grad.size(), grad.type());
        for (int i = 0; i < grad.rows(); i++) {
            for (int j = 0; j < grad.cols(); j++) {
                if (grad.get(i, j)[0] > threshold) {
                    out.put(i, j, 255.0, 255.0, 255.0);
                } else {
                    out.put(i, j, 0, 0, 0);
                }

            }
        }
        return out;
    }

    public static Mat getEdgeDedectionCanny(Mat mat, int threshold) {
        //Canny and Contours finding
        Mat cannyOutput = new Mat();
        Imgproc.Canny(mat, cannyOutput, threshold, threshold * 2);
        return cannyOutput;
    }

    public static Mat getEdgeDedectionSobelAndCanny(Mat mat, int threshold) {
        val sobel = getEdgeDedectionSobel(mat, threshold);
        val cany = getEdgeDedectionCanny(mat, threshold);
        val out = new Mat(mat.size(), sobel.type());

        for (int i = 0; i < out.rows(); i++) {
            for (int j = 0; j < out.cols(); j++) {
                val s = sobel.get(i, j);
                val c = cany.get(i, j);
                if (Arrays.stream(s).sum() == Arrays.stream(c).sum()) {
                    out.put(i, j, s);
                }
            }
        }
        return out;
    }

    public static Mat getLineDedection(Mat mat) {
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(mat, linesP, 1, Math.PI / 180, 255 / 4, mat.size().height / 4.0, mat.size().height / 25.5); // runs the actual detection

        return linesP;
    }

    public static void blob(Mat mat) {
        Mat MatOut = new Mat();

        // make a simpleblob detector:
        SimpleBlobDetector blobby = SimpleBlobDetector.create();
// save the original config:
// (or use the one below)
        blobby.write("data/blob.xml");

        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

        blobby.detect(mat, keypoints1);

        Scalar cores = new Scalar(0, 0, 255);

        Features2d.drawKeypoints(mat, keypoints1, MatOut, cores, 2);

        HighGui.imshow("blob", MatOut);
        HighGui.waitKey();
    }

    public static RotatedRect getGreatestElipse(Mat edgeDetected) {
        val contours = new ArrayList<MatOfPoint>();
        var hierarchy = new Mat();
        Imgproc.findContours(edgeDetected, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        RotatedRect[] minEllipse = new RotatedRect[contours.size()];

        int indexDisplay = 0;


        for (int i = 0; i < contours.size(); i++) {
            minEllipse[i] = new RotatedRect();
            if (contours.get(i).rows() > 5) {
                minEllipse[i] = Imgproc.fitEllipseDirect(new MatOfPoint2f(contours.get(i).toArray()));

            }


            val radius = (int) Math.max(minEllipse[i].size.width, minEllipse[i].size.height) / 2;


            if (minEllipse[i].size.area() > minEllipse[indexDisplay].size.area()
                    && minEllipse[i].center.x - radius >= 0
                    && minEllipse[i].center.y - radius >= 0
                    && minEllipse[i].center.x + radius < edgeDetected.width()
                    && minEllipse[i].center.y + radius < edgeDetected.height()
            ) {
                indexDisplay = i;
            }
        }
//        HighGui.imshow("asas", draw);
//        HighGui.waitKey();
        return minEllipse[indexDisplay];
    }


    static public Mat removeLines(Mat mat, Mat lines) {
        // Draw the lines
        Mat out = mat.clone();
        for (int x = 0; x < lines.rows(); x++) {
            double[] l = lines.get(x, 0);
            Imgproc.line(out, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 0), 1, Imgproc.LINE_8, 0);
        }
        return out;
    }

    public static Mat transponiere(Mat mat, RotatedRect ellipse) {
        Point[] pts = new Point[4];
        ellipse.points(pts);
        return transponiere(mat, Arrays.stream(pts).collect(Collectors.toList()));
    }

    public static Mat transponiere(Mat mat, List<Point> corner) {
        //        https://stackoverflow.com/questions/40688491/opencv-getperspectivetransform-and-warpperspective-java

        //     https://github.com/badlogic/opencv-fun/blob/master/src/pool/tests/PerspectiveTransform.java


        List<Point> target = new ArrayList<Point>();
        target.add(new Point(0, 0));
        target.add(new Point(mat.cols(), 0));
        target.add(new Point(mat.cols(), mat.rows()));
        target.add(new Point(0, mat.rows()));


        Mat cornersMat = Converters.vector_Point2f_to_Mat(corner);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);
        Mat invTrans = Imgproc.getPerspectiveTransform(targetMat, cornersMat);
        Mat proj = new Mat();
        Imgproc.warpPerspective(mat, proj, trans, new Size(mat.cols(), mat.rows()));
        Imgproc.resize(proj, proj, new Size(256, 256));
        return proj;
    }


    public void greatestCircle(Mat input) {

        Mat gray = new Mat();
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.medianBlur(gray, gray, 5);

        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double) gray.rows() / 4, // change this value to detect circles with different distances to each other
                100.0, 30.0, 1, 1000); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(input, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(input, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
        }
        HighGui.imshow("detected circles", input);
        HighGui.waitKey();
        System.exit(0);
    }


    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        BufferedImage x = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        x.setData(bi.getRaster());
        byte[] data = ((DataBufferByte) x.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public static Mat removeAllOutsideEllpipse(Mat input, RotatedRect ellipse) {
        Mat out = Mat.zeros(input.size(), input.type());
        Imgproc.ellipse(out, ellipse, WHITE, -1);


        for (int i = 0; i < input.width(); i++) {
            for (int j = 0; j < input.height(); j++) {
                if (out.get(j, i)[0] == 255) {
                    out.put(j, i, input.get(j, i));
                }
            }
        }
        return out;
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
//        new GaugeExtraction(Imgcodecs.imread("data/example/temp.jpg"));
//        new GaugeExtraction(Imgcodecs.imread("data/example/testBild1.jpg"));
        new GaugeExtraction(Imgcodecs.imread("data/example/Li_Example_1.png"));
    }
}