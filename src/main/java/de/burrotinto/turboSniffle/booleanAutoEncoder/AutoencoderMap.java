package de.burrotinto.turboSniffle.booleanAutoEncoder;

import de.burrotinto.popeye.transformation.Pair;
import lombok.val;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class AutoencoderMap extends HashMap<Mat, Mat> {

    @Override
    public Mat get(Object key) {
        if (super.get(key) != null) {
            return super.get(key);
        } else {
            AtomicReference<Pair<Mat, Integer>> aenhlichstes = new AtomicReference<>();
            this.keySet().parallelStream().forEach(mat -> {
                val dist = BooleanAutoencoder.HAMMINGDISTANZ(mat, (Mat) key);
                if (aenhlichstes.get() == null || aenhlichstes.get().p2 > dist) {
                    aenhlichstes.set(new Pair(mat, dist));
                }
            });
            return super.get(aenhlichstes.get().p1);
        }
    }
}
