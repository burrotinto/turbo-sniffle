package de.burrotinto.turboSniffle.mqtt;

import lombok.SneakyThrows;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

public class MatToMessageString {

    public static String erzeugeStringDarstellung(Mat input) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int r = 0; r < input.rows(); r++) {
            for (int c = 0; c < input.cols(); c++) {
//                for (int i = 0; i < 3 - Integer.toString((int) (input.get(r, c)[0])).length(); i++) {
//                    sb.append(" ");
//                }
                sb.append((int) input.get(r, c)[0]);
                if (c < input.width() - 1) {
                    sb.append(",");
                }
            }
            if (r < input.height() - 1) {
                sb.append(";");
                sb.append("\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static Mat erzeugeMatAusStringDarstellung(String message) {
        String[] rows = message.subSequence(message.indexOf("[") + 1, message.indexOf("]")).toString()
                .replace("\n", "")
                .replace(" ", "")
                .split(";");

        Mat out = new Mat(rows.length, rows[0].split(",").length, 0);
        for (int w = 0; w < out.rows(); w++) {

            int[] row = Arrays.stream(rows[w].split(",")).mapToInt(value -> Integer.valueOf(value)).toArray();
            for (int h = 0; h < out.cols(); h++) {
                out.put(w, h, row[h]);
            }
        }
        return out;
    }

    @SneakyThrows
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        Mat x = Imgcodecs.imread("data/ae/G1000/GarminG1000.png", Imgcodecs.IMREAD_GRAYSCALE);

        FileWriter fileWriter = new FileWriter("data/x.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(erzeugeStringDarstellung(x));
        printWriter.close();

//        Core.bitwise_xor(x, generateMatFromString(generateMessage(x)), x);
        HighGui.imshow("a", erzeugeMatAusStringDarstellung(erzeugeStringDarstellung(x)));
//        HighGui.imshow("",);
        HighGui.waitKey();
    }

    @SneakyThrows
    public static Mat erzeugeMatAusStringDarstellung(byte[] payloadAsBytes) {
        return erzeugeMatAusStringDarstellung(new String(payloadAsBytes, "UTF-8"));
    }
}
