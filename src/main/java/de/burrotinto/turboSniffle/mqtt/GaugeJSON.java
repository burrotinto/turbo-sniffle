package de.burrotinto.turboSniffle.mqtt;

import de.burrotinto.turboSniffle.meters.gauge.Gauge;
import de.burrotinto.turboSniffle.meters.gauge.ValueGauge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GaugeJSON {
    String src;
    String gauge;
    String detected;
    double[] pointer;
    double value;

    public GaugeJSON(ValueGauge gauge){
        this((Gauge) gauge);
        detected = MatToMessageString.generateMessage(gauge.getDrawing(gauge.getSource().clone()));
        pointer = gauge.getPointerAngel();
        value = gauge.getValue();
    }
    public GaugeJSON(Gauge gauge){
        this.gauge =  MatToMessageString.generateMessage(gauge.getSource());
    }
}
