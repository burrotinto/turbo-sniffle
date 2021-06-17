package de.burrotinto.turboSniffle.booleanAutoEncoder;

import de.burrotinto.popeye.transformation.Pair;
import lombok.val;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class BooleanAutoencoderMap<T extends HammingDistanz, D extends HammingDistanz> extends HashMap<T, D> {

    @Override
    public D get(Object key) {
        if (super.get(key) != null) {
            return super.get(key);
        } else {
            AtomicReference<Pair<T, Integer>> aenhlichstes = new AtomicReference<>(new Pair<>(null, Integer.MAX_VALUE));
            this.keySet().forEach(mat -> {
                val dist = mat.getHammingDistanzTo((T) key);

                if (aenhlichstes.get().p2 > dist) {
                    aenhlichstes.set(new Pair(mat, dist));
                }
            });
            return super.get(aenhlichstes.get().p1);
        }
    }
}
