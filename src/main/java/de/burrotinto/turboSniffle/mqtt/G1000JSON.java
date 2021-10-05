package de.burrotinto.turboSniffle.mqtt;

import de.burrotinto.turboSniffle.gauge.GarminG1000;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class G1000JSON {
    Double asi = null;
    Double dg = null;
    Double vsi = null;
    Double alt = null;

    public G1000JSON(GarminG1000 g1000) {
        if (!Double.isNaN(g1000.getAirspeed()))
            asi = g1000.getAirspeed();
        if (!Double.isNaN(g1000.getKurskreisel()))
            dg = g1000.getKurskreisel();
        if (!Double.isNaN(g1000.getVSI()))
            vsi = g1000.getVSI();
        if (!Double.isNaN(g1000.getAltimeter()))
            alt = g1000.getAltimeter();
    }

}
