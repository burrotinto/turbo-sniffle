package de.burrotinto.turboSniffle.booleanAutoEncoder;

import de.burrotinto.popeye.transformation.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class AutoencoderMap extends HashMap<Mat, Mat> {

    private DistanzTypes distanzType;


    @Override
    public Mat get(Object key) {
        if (super.get(key) != null) {
            return super.get(key);
        } else {
            AtomicReference<Pair<Mat, Long>> aenhlichstes = new AtomicReference<>(new Pair<>(null, Long.MAX_VALUE));
            this.keySet().forEach(mat -> {

                val dist = getDistanz(mat, (Mat) key, aenhlichstes.get().p2);


                if (aenhlichstes.get().p2 > dist) {
                    aenhlichstes.set(new Pair(mat, dist));
                }
            });
            return super.get(aenhlichstes.get().p1);
        }
    }

    private Long getDistanz(Mat a, Mat b, Long max) {
        switch (distanzType) {
            case HAMMING:
                return BooleanAutoencoder.HAMMINGDISTANZ(a, b, max);
            default:
                return BooleanAutoencoder.DISTANZ(a, b,1 ,max);
        }
    }
}
