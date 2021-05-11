//package de.burrotinto.popeye.transformation;
//
////import com.asprise.ocr.Ocr;
//import lombok.SneakyThrows;
//import org.opencv.core.*;
////import org.opencv.highgui.HighGui;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.util.Random;
//
//
//@Component
//public class OpenCvInitializerComponent {
//    private static final Logger logger = LoggerFactory.getLogger(OpenCvInitializerComponent.class);
//
//    @Autowired
//    private CircleExtractor circleExtractor;
//    @Autowired
//    private RectangleExtractor rectangleExtractor;
//
//    @PostConstruct
//    private void init() {
//        logger.info("Welcome to OpenCV " + Core.VERSION);
//
////        Mat img = Imgcodecs.imread("sixpacks/sixpack.jpg");
//        Mat img = Imgcodecs.imread("pic/analog/unnamed.jpg");
//
//        // Load an image
//        Mat src = img.clone();
//
//        if (img.empty()){
//            System.out.println("LEER");
//        }
//        circleExtractor.getAllCircles(src).forEach(mat -> {
////            Ocr.setUp();
////            Ocr ocr = new Ocr(); // create a new OCR engine
////            ocr.startEngine("eng", Ocr.SPEED_FASTEST); // English
////            String s = ocr.recognize(Mat2BufferedImage(circleExtractor.prepareForOCR(mat)),
////                    Ocr.RECOGNIZE_TYPE_ALL, Ocr.OUTPUT_FORMAT_PLAINTEXT); // PLAINTEXT | XML | PDF | RTF
////            System.out.println("Result: " + s);
////            ocr.stopEngine();
//            Mat grayimg = mat.clone();
//            Imgproc.cvtColor(mat,grayimg, Imgproc.COLOR_BGR2GRAY);
//            Imgcodecs.imwrite("pic/"+System.currentTimeMillis()+".jpg", new FindPointer().getLineSegmentDetector(grayimg,1,3.14 / 180,150));
//
////            Imgcodecs.imwrite("sixpacks/round"+rnd.nextInt()+".jpg", mat);
//        });
//
//
////        Mat src2 = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
////        // Check if image is loaded fine
////        if (src.empty()) {
////            System.out.println("Error opening image!");
////            System.out.println("Program Arguments: [image_name -- default "
////                    + default_file + "] \n");
////            System.exit(-1);
////        }
////        Mat gray = new Mat();
////        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
////        Imgproc.medianBlur(gray, gray, 5);
////        Mat circles = new Mat();
////        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
////                (double) gray.rows() / 16, // change this value to detect circles with different distances to each other
////                100.0, 30.0, 60, 100); // change the last two parameters
////        // (min_radius & max_radius) to detect larger circles
////        for (int x = 0; x < circles.cols(); x++) {
////            double[] c = circles.get(0, x);
////            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
////            // circle center
////            Imgproc.circle(src, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
////            // circle outline
////            int radius = (int) Math.round(c[2]);
////            Imgproc.circle(src, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
////
////            Mat newImage = src2.submat((int) Math.round(center.y - radius), (int) Math.round(center.y + radius), (int) Math.round(center.x - radius), (int) Math.round(center.x + radius));
////            Mat resizeimage = new Mat();
////            Size sz = new Size(200,200);
////            Imgproc.resize( newImage, resizeimage, sz );
////
////
////            Imgproc.cvtColor(resizeimage, resizeimage, Imgproc.COLOR_BGR2GRAY);
////            Imgproc.blur(resizeimage, resizeimage, new Size(5, 5));
////
////            Imgproc.Canny(resizeimage, resizeimage, 10, 90, 3, true);
////
////            HighGui.imshow("detected circles " + x, resizeimage);
////            Imgcodecs.imwrite("round"+x+".jpg", img);
////        }
//
////        HighGui.waitKey();
//        Imgcodecs.imwrite("sixpack2.jpg", img);
//    }
//
//    @SneakyThrows
//    static BufferedImage Mat2BufferedImage(Mat matrix) {
//        MatOfByte mob = new MatOfByte();
//        Imgcodecs.imencode(".png", matrix, mob);
//        byte ba[] = mob.toArray();
//
//        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
//        return bi;
//    }
//
//    public Mat getHoughPTransform(Mat image, double rho, double theta, int threshold) {
//        Mat result = image.clone();
//        Mat lines = new Mat();
//        Imgproc.HoughLinesP(image, lines, rho, theta, threshold);
//
//        for (int i = 0; i < lines.cols(); i++) {
//            double[] val = lines.get(0, i);
//            Imgproc.line(result, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 0, 255), 2);
//        }
//        return result;
//    }
//}