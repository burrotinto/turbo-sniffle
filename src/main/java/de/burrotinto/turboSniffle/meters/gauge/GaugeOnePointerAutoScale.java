package de.burrotinto.turboSniffle.meters.gauge;

import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.cv.TextDedection;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class GaugeOnePointerAutoScale extends GaugeOnePointer {

    private final TextDedection textDedection;

    GaugeOnePointerAutoScale(Gauge gauge, TextDedection textDedection, Optional<Double> steps, Optional<Double> min, Optional<Double> max) throws NotGaugeWithPointerException {
        super(gauge, steps, min, max);
        this.textDedection = textDedection;

        // Beschriftung erkennung
        val textAreas = textDedection.getTextAreas(otsu);
        textAreas.addAll(textDedection.getTextAreas(source));
        Collections.shuffle(textAreas);

        Mat ideal = otsu.clone();

        //Alle erkannten TExxtfelder die sich in der Äusseren Hälfte befinden
        for (RotatedRect r : textAreas.stream().filter(rotatedRect -> Helper.calculateDistanceBetweenPointsWithPoint2D(rotatedRect.center, getCenter()) >= getRadius() / 2).collect(Collectors.toList())) {
            try {
                BufferedImage sub = Helper.Mat2BufferedImage(otsu.submat(r.boundingRect()));
                String str = textDedection.doOCRNumbers(sub);
                Double i = Double.parseDouble(str);
                if (addToScaleMark(r, i)) {
                    System.out.println("OTSU_OCR =" + i);
                    Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                }
            } catch (Exception e) {
                try {
                    BufferedImage sub2 = Helper.Mat2BufferedImage(source.submat(r.boundingRect()));
                    String str2 = textDedection.doOCRNumbers(sub2);
                    Double i2 = Double.parseDouble(str2);
                    if (addToScaleMark(r, i2)) {
                        System.out.println("GREYSCAKLE_OCR=" + i2);
                        Helper.drawRotatedRectangle(ideal, r, Helper.WHITE);
                    }
                } catch (Exception e2) {
                }
            }
        }

        //Check ob über MIN/MAX etwas ermittelt werden kann
        if (labelScale.size() <= 1) {
            if (min.isPresent() && max.isPresent()) {
                addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(225, getRadius()), new Size(10, 10), 0), min.get());
                addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(315, getRadius()), new Size(10, 10), 0), max.get());
                addToScaleMarkFORCE(new RotatedRect(poolarZuBildkoordinaten(90, getRadius()), new Size(10, 10), 0), (max.get() + min.get()) / 2); //Kann SEIN Das WERT nicht EXISTIERT
            } else {
                //Keine Möglichkeit etwas zu generieren
                throw new NotGaugeWithPointerException();
            }
        }

        // Anhand von MAX auf MIN schließen
        if (min.isPresent() && max.isPresent() && !labelScale.containsValue(min.get()) && labelScale.containsValue(max.get())) {
            double maxW = bildkoordinatenZuPoolar(labelScale.entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue())).get().getKey().center);
            if (maxW > 0 && maxW < 180) {
                addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 90, getRadius()), new Size(10, 10), 0), min.get());
            } else {
                addToScaleMark(new RotatedRect(poolarZuBildkoordinaten(maxW - 90, getRadius()), new Size(10, 10), 0), min.get());
            }
        }

        setIdealisierteDarstellung(ideal);
    }
}
