package de.burrotinto.turboSniffle.cv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Point;

@Getter
@Setter
@AllArgsConstructor
public class Circle {
    private Point center;
    private int radius;
}
