package de.burrotinto.turboSniffle.gauge;

import org.opencv.core.Mat;

public class Cessna172SixpackFactory{

    public static Cessna172AirspeedIndecator getCessna172AirspeedIndecator(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172AirspeedIndecator(GaugeFactory.getGaugeWithHeatMap(src, -1), GaugeFactory.TEXT_DEDECTION_Numbers);
    }

    public static Cessna172VerticalSpeedIndicator getCessna172VerticalSpeedIndicator(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172VerticalSpeedIndicator(GaugeFactory.getGaugeWithHeatMap(src, -1),  GaugeFactory.TEXT_DEDECTION_Numbers);
    }

    public static TwoPointerValueGauge getCessna172Altimeter(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172AltimeterIndecator(GaugeFactory.getGaugeWithHeatMap(src, -1),  GaugeFactory.TEXT_DEDECTION_Numbers);
    }

    public static Cessna172KurskreiselIndecator getCessna172Kurskreisel(Mat src) throws NotGaugeWithPointerException {
        return new Cessna172KurskreiselIndecator(GaugeFactory.getGaugeWithHeatMap(src, -1));
    }
}
