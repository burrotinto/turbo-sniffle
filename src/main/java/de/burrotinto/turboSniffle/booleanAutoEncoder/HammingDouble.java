package de.burrotinto.turboSniffle.booleanAutoEncoder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HammingDouble implements HammingDistanz {

    public Double value;

    @Override
    public int getHammingDistanzTo(HammingDistanz other) {
        return other instanceof HammingDouble ? ((HammingDouble) other).getHammingDistanzTo(this) : other.getHammingDistanzTo(this);
    }

    public int getHammingDistanzTo(HammingDouble other) {
        return (int) Math.abs(other.value - this.value);
    }
}
