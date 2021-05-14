package de.burrotinto.popeye.transformation.examples;

import de.burrotinto.popeye.meters.Pointer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class FindContours2 {
    private Mat srcGray = new Mat();
    private JFrame frame;
    private JLabel imgSrcLabel;
    private JLabel imgContoursLabel;
    private static final int MAX_THRESHOLD = 758;
    private int threshold = 120;
    private Random rng = new Random();

    public FindContours2(Mat src) {
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

    public FindContours2(String file) {
        this(Imgcodecs.imread(file));
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

    private void update() {
        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        List<Pointer> pointers = new LinkedList<>();

        for (MatOfPoint contour : contours) {
            Pointer.isPointer(contour).ifPresent(pointers::add);
        }

        Mat drawing = srcGray.clone();


        for (Pointer point : pointers) {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.line(drawing, point.getDirection().p1, point.getDirection().p2, color, 2);
            Imgproc.putText(drawing, "[" + point.scale() + "]", point.getDirection().p2, 2, 0.5, color);

            // rotated rectangle
            Point[] rectPoints = new Point[4];
            point.getMinRect().points(rectPoints);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(drawing, rectPoints[j], rectPoints[(j+1) % 4], color);
            }
        }


        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(drawing)));
        frame.repaint();
        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(drawing)));
        frame.repaint();
    }

    public static void main(String[] args) {
        // Load the native OpenCV library
        nu.pattern.OpenCV.loadLocally();
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FindContours2("sixpacks/robinDR400_KMH.JPG");
//                new CircleExtractor().getAllCircles(Imgcodecs.imread("sixpacks/robindr400.JPG")).forEach(FindContours2::new);

            }
        });
    }
}
