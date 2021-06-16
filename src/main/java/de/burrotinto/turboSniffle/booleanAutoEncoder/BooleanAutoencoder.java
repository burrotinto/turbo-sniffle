package de.burrotinto.turboSniffle.booleanAutoEncoder;

import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class BooleanAutoencoder {
    private final AutoencoderMap encodeMap = new AutoencoderMap();
    private final AutoencoderMap decodeMap = new AutoencoderMap();

    public Mat decode(Mat mat) {
        return decodeMap.get(mat);
    }

    public Mat encode(Mat mat) {
        return encodeMap.get(mat);
    }


    public Mat classify(Mat mat){
        return decode(encode(mat));
    }



    public void learn(List<Mat> learnings){
        val p = learnings.size()/2;

        learnings.forEach(mat -> {
            val reduct = mat.clone();
            Imgproc.resize(mat, reduct, new Size(p, p));
            encodeMap.put(mat.clone(),reduct);
        });

        //decode
        encodeMap.entrySet().forEach(matMatEntry -> decodeMap.put(matMatEntry.getValue(),matMatEntry.getKey()));
    }

    @SneakyThrows
    public static int HAMMINGDISTANZ(Mat a, Mat b) {
        int distanz = 0;
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.cols(); j++) {
                if (a.get(i, j)[0] != b.get(i, j)[0]) {
                    distanz++;
                }
            }
        }
        return distanz;
    }

    @SneakyThrows
    public static long HAMMINGDISTANZ(Mat a, Mat b,long max) {
        long distanz = 0;
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.cols(); j++) {
                if (a.get(i, j)[0] != b.get(i, j)[0]) {
                    distanz++;
                    if(distanz > max){
                        return distanz;
                    }
                }
            }
        }
        return distanz;
    }

    @SneakyThrows
    public static long DISTANZ(Mat a, Mat b,long max) {
        int distanz = 0;
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.cols(); j++) {
                distanz += Math.abs(a.get(i, j)[0] - b.get(i, j)[0]);
                if(distanz > max){
                    return distanz;
                }
            }
        }
        return distanz;
    }
}
