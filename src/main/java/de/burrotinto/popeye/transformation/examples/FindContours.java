package de.burrotinto.popeye.transformation.examples;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.burrotinto.popeye.transformation.Helper;
import de.burrotinto.popeye.transformation.Pair;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

class FindContours {
    private Mat srcGray = new Mat();
    private JFrame frame;
    private JLabel imgSrcLabel;
    private JLabel imgContoursLabel;
    private static final int MAX_THRESHOLD = 758;
    private int threshold = 130;
    private Random rng = new Random(12345);

    public FindContours(String file) {
        String filename = file;
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(srcGray, srcGray, new Size(3, 3));
        // Create and set up the window.
        frame = new JFrame("Finding contours in your image demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(src);
        addComponentsToPane(frame.getContentPane(), img);
        // Use the content pane's default BorderLayout. No need for
        // setLayout(new BorderLayout());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
        update();
    }

    private void addComponentsToPane(Container pane, Image img) {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(new JLabel("Canny threshold: "));
        JSlider slider = new JSlider(0, MAX_THRESHOLD, threshold);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                threshold = source.getValue();
                update();
            }
        });
        sliderPanel.add(slider);
        pane.add(sliderPanel, BorderLayout.PAGE_START);
        JPanel imgPanel = new JPanel();
        imgSrcLabel = new JLabel(new ImageIcon(img));
        imgPanel.add(imgSrcLabel);
        Mat blackImg = Mat.zeros(srcGray.size(), CvType.CV_8U);
        imgContoursLabel = new JLabel(new ImageIcon(HighGui.toBufferedImage(blackImg)));
        imgPanel.add(imgContoursLabel);
        pane.add(imgPanel, BorderLayout.CENTER);
    }
//    private void update() {
//        Mat cannyOutput = new Mat();
//        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//        Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
//        for (int i = 0; i < contours.size(); i++) {
//            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
//            Imgproc.drawContours(drawing, contours, i, color, 2, Core.NORM_L1, hierarchy, 0, new Point());
//        }
//        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(drawing)));
//        frame.repaint();
//    }

    private void update() {
        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);


        RotatedRect[] minRect = new RotatedRect[contours.size()];
        List<Moments> mu = new ArrayList<>(contours.size());

        for (int i = 0; i < contours.size(); i++) {
            minRect[i] = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            mu.add(Imgproc.moments(contours.get(i)));
        }

        List<Point> mc = new ArrayList<>(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            //add 1e-5 to avoid division by zero
            mc.add(new Point(mu.get(i).m10 / (mu.get(i).m00 + 1e-5), mu.get(i).m01 / (mu.get(i).m00 + 1e-5)));
        }
        Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
//        Mat drawing = srcGray.clone();

        List<MatOfPoint> hullList = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }

        // getMaxScaleObject
        int maxSO = 0;
        double maxScale = 0;
        for (int i = 0; i < contours.size(); i++) {
            //Länge und breite des Objectes
            double l = Math.max(minRect[i].size.height, minRect[i].size.width);
            double b = Math.min(minRect[i].size.height, minRect[i].size.width);
            if(l/b > maxScale
                    &&  Double.isFinite(l/b)
                    && !Double.isNaN(l/b)
            && cannyOutput.size().height *0.2 < l){
                maxScale = l/b;
                maxSO = i;
            }

        }

        for (int i = 0; i < contours.size(); i++) {
//            if (i== maxSO) {
            if (Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) > 100) {

                double l = Math.max(minRect[i].size.height, minRect[i].size.width);
                double b = Math.min(minRect[i].size.height, minRect[i].size.width);
                double scale = l/b;

                Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));

                Imgproc.drawContours(drawing, hullList, i, color);
                Pair<Point> max = Helper.maxDistance(contours.get(i).toList());
//                Imgproc.line(drawing,Helper.pointAtX(max.p1,max.p2,0),max.p2,color);
                Imgproc.putText(drawing, "[" + i + "|"+ scale + "]", hullList.get(i).toArray()[0], 2, 0.5, color);

                // rotated rectangle
                Point[] rectPoints = new Point[4];
                minRect[i].points(rectPoints);
                for (int j = 0; j < 4; j++) {
                    Imgproc.line(drawing, rectPoints[j], rectPoints[(j+1) % 4], color);
                }
            }
        }

        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(drawing)));
        frame.repaint();

        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(drawing)));
        frame.repaint();
        System.out.println("\t Info: Area and Contour Length \n");
        for (int i = 0; i < contours.size(); i++) {
//            if (i== maxSO) {
            if (Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) > 100) {
                System.out.format(" * Contour[%d] - Area (M_00) = %.2f - Area OpenCV: %.2f - Length: %.2f\n", i,
                        mu.get(i).m00, Imgproc.contourArea(contours.get(i)),
                        Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), false));
            }
        }
//        HighGui.waitKey();
//        System.exit(0);
    }

    public static void main(String[] args) {
        // Load the native OpenCV library
        nu.pattern.OpenCV.loadLocally();
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new FindContours("pic/analog/analogeanzeigederkraftstoffmenge.jpg");
                new FindContours("sixpacks/sixpack.jpg");
            }
        });
    }
}
