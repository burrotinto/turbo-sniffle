package de.burrotinto.turboSniffle.mqtt;

import de.burrotinto.turboSniffle.gauge.Gauge;
import de.burrotinto.turboSniffle.gauge.ValueGauge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GaugeJSON {
    String src;
    String gauge;
    String idealisierteDarstellung;
    String detected;
    double[] pointer;
    double value;

    public GaugeJSON(ValueGauge gauge) {
        this((Gauge) gauge);
        detected = MatToMessageString.erzeugeStringDarstellung(gauge.getDrawing(gauge.getSource().clone()));
        pointer = gauge.getPointerAngel();
        value = gauge.getValue();
        idealisierteDarstellung = MatToMessageString.erzeugeStringDarstellung(gauge.getIdealisierteDarstellung());
    }

    public GaugeJSON(Gauge gauge) {
        this.gauge = MatToMessageString.erzeugeStringDarstellung(gauge.getSource());
    }
}
