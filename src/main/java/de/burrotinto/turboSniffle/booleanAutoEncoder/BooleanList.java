package de.burrotinto.turboSniffle.booleanAutoEncoder;

import java.util.ArrayList;

public class BooleanList extends ArrayList<Boolean> implements HammingDistanz {

    @Override
    public int getHammingDistanzTo(HammingDistanz other) {
        return other instanceof BooleanList ? ((BooleanList) other).getHammingDistanzTo(this) : other.getHammingDistanzTo(this);
    }

    public int getHammingDistanzTo(BooleanList other) {
        int dist = Math.abs(other.size() - size());
        for (int i = 0; i < Math.min(other.size(), size()); i++) {
            if (other.get(i) != this.get(i)) {
                dist++;
            }
        }
        return dist;
    }
}
