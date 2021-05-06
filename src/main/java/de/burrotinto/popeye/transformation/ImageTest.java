package de.burrotinto.popeye.transformation;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//@Component
public class ImageTest implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        //        BufferedImage img = null;
//        try {
//            img = ImageIO.read(new File("https://eu-browse.startpage.com/av/anon-image?piurl=https%3A%2F%2Fjoemarino94.tripod.com%2Fsitebuildercontent%2Fsitebuilderpictures%2F.pond%2FSixPack.JPG.w560h420.jpg&sp=1620235007T3e0eafd6bae2a9491866d177de6ec735de446103b3ffb937e3502677b74aedf5"));
//        } catch (IOException e) {
//        }
        Mat img = Imgcodecs.imread("F:\\Florian\\studium\\Abschlussarbeit\\popeye\\sixpack.jpg");

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(img, img, new Size(3, 3));
//        Imgproc.Canny(img, img, 0, 3, 3, false);
    }
}
