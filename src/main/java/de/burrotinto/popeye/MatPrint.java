package de.burrotinto.popeye;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

public class MatPrint {
    public static String getMatAsString(Mat mat) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < mat.rows(); x++) {
            for (int y = 0; y < mat.cols(); y++) {
                Arrays.stream(mat.get(x, y)).forEach(value -> sb.append(value).append(", "));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static BufferedImage toBufferdImage(Mat mat) {
        // Read image to Mat as before
        Mat rgba = mat.clone();
//        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGB2GRAY, 0);

// Create an empty image in matching format
        BufferedImage gray = new BufferedImage(rgba.width(), rgba.height(), BufferedImage.TYPE_BYTE_GRAY);

// Get the BufferedImage's backing array and copy the pixels directly into it
        byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
        rgba.get(0, 0, data);
        return gray;
    }
}
