package de.burrotinto.turboSniffle.mqtt;

import de.burrotinto.turboSniffle.gauge.GarminG1000;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class G1000JSON {
    double asi;
    double dg;
    double vsi;
    double alt;

    public G1000JSON(GarminG1000 g1000) {
        asi = g1000.getAirspeed();
        dg = g1000.getKurskreisel();
        vsi = g1000.getVSI();
        alt = g1000.getAltimeter();
    }

}
