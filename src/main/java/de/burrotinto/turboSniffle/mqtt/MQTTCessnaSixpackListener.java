package de.burrotinto.turboSniffle.mqtt;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.burrotinto.turboSniffle.gauge.Cessna172SixpackFactory;
import de.burrotinto.turboSniffle.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.gauge.ValueGauge;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Locale;


@Component
@RequiredArgsConstructor
public class MQTTCessnaSixpackListener extends MQTTListener {
    @NonNull
    private MQTTConfig mqttConfig;
    @NonNull
    private Gson gson;

    @SneakyThrows
    @Override
    public void newMessage(Mqtt5Publish publish) {
        super.newMessage(publish);

        String topic = publish.getTopic().toString()
                .replace("/greyscale", "");

        ValueGauge gauge;

        switch (publish.getTopic().getLevels().get(3).toLowerCase(Locale.ROOT)) {
            case "asi":
                gauge = Cessna172SixpackFactory.getCessna172AirspeedIndecator(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes()));
                break;
            case "dg":
                gauge = Cessna172SixpackFactory.getCessna172Kurskreisel(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes()));
                break;
            case "vsi":
                gauge = Cessna172SixpackFactory.getCessna172VerticalSpeedIndicator(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes()));
                break;
            case "alt":
                gauge = Cessna172SixpackFactory.getCessna172Altimeter(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes()));
                break;
            default:
                gauge = GaugeFactory.getGaugeWithOnePointerAutoScale(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes()));
        }
        try {
            GaugeJSON json = new GaugeJSON(gauge);

            mqttClient.publish(topic,json);

        } catch (Exception e) {
            e.printStackTrace();
            mqttClient.publish(topic + "/EXCEPTION", e.getLocalizedMessage());
        }


    }


    @Override
    public String[] getSubscribeTopic() {
        return new String[]{
                mqttConfig.getBaseTopic() + "/cessna172/+/asi/greyscale",
                mqttConfig.getBaseTopic() + "/cessna172/+/vsi/greyscale",
                mqttConfig.getBaseTopic() + "/cessna172/+/dg/greyscale",
                mqttConfig.getBaseTopic() + "/cessna172/+/alt/greyscale"
        };
    }
}
