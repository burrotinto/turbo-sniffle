package de.burrotinto.turboSniffle.gauge;

public class GaugeOnePointerNoScale extends ValueGauge {
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
