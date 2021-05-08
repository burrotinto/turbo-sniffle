//package de.burrotinto.popeye.transformation;
//
//import net.sourceforge.tess4j.ITesseract;
//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;
//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfInt;
//import org.opencv.core.MatOfPoint;
//import org.opencv.core.MatOfPoint2f;
//import org.opencv.core.RotatedRect;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.highgui.HighGui;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
////@Component
//public class OCRTest implements InitializingBean {
//
//    private static final int
//            CV_MOP_CLOSE = 3,
//            CV_THRESH_OTSU = 8,
//            CV_THRESH_BINARY = 0,
//            CV_ADAPTIVE_THRESH_GAUSSIAN_C  = 1,
//            CV_ADAPTIVE_THRESH_MEAN_C = 0,
//            CV_THRESH_BINARY_INV  = 1;
//
//
//    public static boolean checkRatio(RotatedRect candidate) {
//        double error = 0.3;
//        //Spain car plate size: 52x11 aspect 4,7272
//        //Russian 12x2 (52cm / 11.5)
//        //double aspect = 52/11.5;
//        double aspect = 6;
//        int min = 15 * (int)aspect * 15;
//        int max = 125 * (int)aspect * 125;
//        //Get only patchs that match to a respect ratio.
//        double rmin= aspect - aspect*error;
//        double rmax= aspect + aspect*error;
//        double area= candidate.size.height * candidate.size.width;
//        float r= (float)candidate.size.width / (float)candidate.size.height;
//        if(r<1)
//            r= 1/r;
//        if(( area < min || area > max ) || ( r < rmin || r > rmax )){
//            return false;
//        }else{
//            return true;
//        }
//    }
//
//    public static boolean checkDensity(Mat candidate) {
//        float whitePx = 0;
//        float allPx = 0;
//        whitePx = Core.countNonZero(candidate);
//        allPx = candidate.cols() * candidate.rows();
//        //System.out.println(whitePx/allPx);
//        if (0.62 <= whitePx/allPx)
//            return true;
//        else
//            return false;
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//
//        Mat img = new Mat();
//        Mat imgGray = new Mat();
//        Mat imgGaussianBlur = new Mat();
//        Mat imgSobel = new Mat();
//        Mat imgThreshold = new Mat();
//        Mat imgAdaptiveThreshold = new Mat();
//        Mat imgAdaptiveThreshold_forCrop = new Mat();
//        Mat imgMoprhological = new Mat();
//        Mat imgContours = new Mat();
//        Mat imgMinAreaRect = new Mat();
//        Mat imgDetectedPlateCandidate = new Mat();
//        Mat imgDetectedPlateTrue = new Mat();
//
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//
//        img = Imgcodecs.imread("sixpack.jpg");
//        //Imgcodecs.imwrite("changes/st/1_True_Image.png", img);
//
//        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
//        //Imgcodecs.imwrite("changes/st/2_imgGray.png", imgGray);
//
//        Imgproc.GaussianBlur(imgGray,imgGaussianBlur, new Size(3, 3),0);
//        //Imgcodecs.imwrite("changes/st/3_imgGaussianBlur.png", imgGray);
//
//        Imgproc.Sobel(imgGaussianBlur, imgSobel, -1, 1, 0);
//        //Imgcodecs.imwrite("changes/st/4_imgSobel.png", imgSobel);
//
//        Imgproc.threshold(imgSobel, imgThreshold, 0, 255,  CV_THRESH_OTSU + CV_THRESH_BINARY);
//        //Imgcodecs.imwrite("changes/st/5_imgThreshold.png", imgThreshold);
//
//        Imgproc.adaptiveThreshold(imgSobel, imgAdaptiveThreshold, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 75, 35);
//        //Imgcodecs.imwrite("changes/st/5_imgAdaptiveThreshold.png", imgAdaptiveThreshold);
//
//        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold_forCrop, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 99, 4);
//        //Imgcodecs.imwrite("changes/st/5_imgAdaptiveThreshold_forCrop.png", imgAdaptiveThreshold_forCrop);
//
////        Mat element = getStructuringElement(MORPH_RECT, new Size(17, 3));
////        Imgproc.morphologyEx(imgAdaptiveThreshold, imgMoprhological, CV_MOP_CLOSE, element); //или imgThreshold
//        //Imgcodecs.imwrite("changes/st/6_imgMoprhologicald.png", imgMoprhological);
//
//        HighGui.imshow("detected circles", imgAdaptiveThreshold_forCrop);
//        HighGui.waitKey();
//
//        imgContours = imgMoprhological.clone();
//        Imgproc.findContours(imgContours,
//                contours,
//                new Mat(),
//                Imgproc.RETR_LIST,
//                Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(imgContours, contours, -1, new Scalar(255,0,0));
//        //Imgcodecs.imwrite("changes/st/7_imgContours.png", imgContours);
//
//        imgMinAreaRect = img.clone();
//        if (contours.size() > 0) {
//            for (MatOfPoint matOfPoint : contours) {
//                MatOfPoint2f points = new MatOfPoint2f(matOfPoint.toArray());
//
//                RotatedRect box = Imgproc.minAreaRect(points);
//                if(checkRatio(box)){
//                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 0, 255));
//                    imgDetectedPlateCandidate = new Mat(imgAdaptiveThreshold_forCrop, box.boundingRect());
//                    if(checkDensity(imgDetectedPlateCandidate))
//                        imgDetectedPlateTrue = imgDetectedPlateCandidate.clone();
//                }
//                else
//                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 255, 0));
//            }
//        }
//
//        //Imgcodecs.imwrite("changes/st/8_imgMinAreaRect.png", imgMinAreaRect);
//        //Imgcodecs.imwrite("changes/st/9_imgDetectedPlateCandidate.png", imgDetectedPlateCandidate);
//        Imgcodecs.imwrite("x.png", imgAdaptiveThreshold_forCrop);
//
//        File imageFile = new File("x.png");
//        ITesseract instance = new Tesseract();
//        instance.setLanguage("eng");
//        instance.setTessVariable("tessedit_char_whitelist", "0123456789");
//        try {
//            String result = instance.doOCR(imageFile);
//            System.out.println(result);
//        } catch (TesseractException e) {
//            System.err.println(e.getMessage());
//        }
//    }
////    @Override
////    public void afterPropertiesSet() throws Exception {
//////        https://stackoverflow.com/questions/37302098/image-preprocessing-with-opencv-before-doing-character-recognition-tesseract
//////        1) Source Image
////        Mat img =  Imgcodecs.imread("sixpack.jpg");
////        Imgcodecs.imwrite("preprocess/True_Image.png", img);
////        HighGui.imshow("Orginal" , img);
////
//////        2) Gray Scale
////        Mat imgGray = new Mat();
////        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
////        Imgcodecs.imwrite("preprocess/Gray.png", imgGray);
////        HighGui.imshow("Grey" , imgGray);
////
//////        3) Gaussian Blur
////        Mat imgGaussianBlur = new Mat();
////        Imgproc.GaussianBlur(imgGray,imgGaussianBlur,new Size(3, 3),0);
////        Imgcodecs.imwrite("preprocess/gaussian_blur.png", imgGaussianBlur);
////        HighGui.imshow("gaussian_blur" , imgGaussianBlur);
////
//////        4) Adaptive Threshold
////        Mat imgAdaptiveThreshold = new Mat();
////        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C ,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 99, 4);
////        Imgcodecs.imwrite("preprocess/adaptive_threshold.png", imgAdaptiveThreshold);
////        HighGui.imshow("adaptive_threshold" , imgAdaptiveThreshold);
////
////
////
////        HighGui.waitKey();
////        System.exit(0);
////    }
//}
