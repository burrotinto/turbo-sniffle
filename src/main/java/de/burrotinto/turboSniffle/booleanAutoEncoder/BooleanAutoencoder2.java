package de.burrotinto.turboSniffle.booleanAutoEncoder;

import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.Map;

public class BooleanAutoencoder2<D extends HammingDistanz> {
    private static final double[] SCALAR = new double[]{255.0, 255.0, 255.0};
    private final BooleanAutoencoderMap<BooleanList, D> encodeMap = new BooleanAutoencoderMap();
    private final BooleanAutoencoderMap<D, BooleanList> decodeMap = new BooleanAutoencoderMap();

    private final int n;

    public BooleanAutoencoder2(int n) {
        this.n = n;
    }

    private BooleanList groesseAnpassen(Mat mat) {
        val m = mat.clone();
        Imgproc.resize(m, m, new Size(Math.pow(n, 0.5), Math.pow(n, 0.5)));
        BooleanList x = new BooleanList();
        int l = 0;
        for (int i = 0; i < m.width(); i++) {
            for (int j = 0; j < m.height(); j++) {
                val y = m.get(i, j);
                x.add(m.get(i, j)[0] > 16);
            }
        }
        return x;
    }

    private Mat groesseAnpassen(BooleanList list) {
        int l = (int) Math.pow(list.size(), 0.5);


        val m = Mat.zeros(new Size(l, l), 0);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)) {
                m.put(i / l, i % l, SCALAR);
            }
        }

        return m;
    }

    public Mat decode(D d) {
        return groesseAnpassen(decodeMap.get(d));
    }

    public D encode(Mat x) {
        return encodeMap.get(groesseAnpassen(x));
    }

    public Mat doAutoencoderStuff(Mat mat) {
        HighGui.imshow("aa", mat);
        HighGui.waitKey();
        return decode(encode(mat));
    }

    public void learn(Map<Mat, D> learnings) {

        learnings.forEach((mat, hamming) -> {
            encodeMap.put(groesseAnpassen(mat), hamming);
        });

        //decode
        encodeMap.entrySet().forEach(matMatEntry -> decodeMap.put(matMatEntry.getValue(), matMatEntry.getKey()));
    }

}
