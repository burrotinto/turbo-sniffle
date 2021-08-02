package de.burrotinto.turboSniffle.arbeit;


import lombok.Getter;
import lombok.val;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Getter
public class ExampleFile {
    private Optional<Double> value = Optional.empty();
    private Optional<Double> min = Optional.empty();
    private Optional<Double> max = Optional.empty();
    private Optional<Double> steps = Optional.empty();

    public ExampleFile(String string) {
        if (string.split("_").length > 1) {
            val values = Arrays.asList(string.split("_"));
            values.forEach(s -> {
                val key = s.split("=")[0];
                val value = s.split("=")[1];
                switch (key.toLowerCase(Locale.ROOT)) {
                    case "value":
                        this.value = Optional.ofNullable(Double.valueOf(value.replace(",", ".")));
                        break;
                    case "min":
                        this.min = Optional.ofNullable(Double.valueOf(value));
                        break;
                    case "max":
                        this.max = Optional.ofNullable(Double.valueOf(value));
                        break;
                    case "step":
                        this.steps = Optional.ofNullable(Double.valueOf(value));
                        break;
                }
            });
        }
    }
}
