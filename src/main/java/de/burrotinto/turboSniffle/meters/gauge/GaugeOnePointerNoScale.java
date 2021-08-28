package de.burrotinto.turboSniffle.meters.gauge;

public class GaugeOnePointerNoScale extends AutoEncoderGauge {
    GaugeOnePointerNoScale(Gauge gauge) throws NotGaugeWithPointerException {
        super(gauge);
    }

    /**
     * Gibt nur den Winkel zurück
     * @return Winkel des Zeigers
     */
    @Override
    public double getValue() {
        return getPointerAngel()[0];
    }
}
