package de.burrotinto.turboSniffle.mqtt;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.burrotinto.turboSniffle.meters.gauge.Cessna172SixpackFactory;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.meters.gauge.ValueGauge;
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
                gauge = Cessna172SixpackFactory.getCessna172AirspeedIndecator(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes()));
                break;
            case "dg":
                gauge = Cessna172SixpackFactory.getCessna172Kurskreisel(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes()));
                break;
            case "vsi":
                gauge = Cessna172SixpackFactory.getCessna172VerticalSpeedIndicator(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes()));
                break;
            case "alt":
                gauge = Cessna172SixpackFactory.getCessna172Altimeter(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes()));
                break;
            default:
                gauge = GaugeFactory.getGaugeWithOnePointerAutoScale(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes()));
        }
        try {
            GaugeJSON json = new GaugeJSON(gauge);
            json.src = new String(publish.getPayloadAsBytes(), "UTF-8");

            mqttClient.publish(topic,json);

//            mqttClient.publish(topic, gson.toJson(json));
//            if (publish.getTopic().getLevels().get(3).toLowerCase(Locale.ROOT).equals("alt")) {
//                mqttClient.publish(topic + "/pointerAngle100", String.valueOf(json.getPointer()[1]));
//                mqttClient.publish(topic + "/pointerAngle1000", String.valueOf(json.getPointer()[0]));
//            } else {
//                mqttClient.publish(topic + "/pointerAngle", String.valueOf(json.getPointer()[0]));
//            }
//
//            mqttClient.publish(topic + "/value", String.valueOf(json.value));
//            mqttClient.publish(topic + "/detected", json.detected);
//            mqttClient.publish(topic + "/gauge", json.gauge);

        } catch (Exception e) {
            e.printStackTrace();
            mqttClient.publish(topic + "/EXCEPTION", e.getLocalizedMessage());
        }


    }


    @Override
    public String[] getSubscribeTopic() {
        return new String[]{
                mqttConfig.getTopic() + "/cessna172/+/asi/greyscale",
                mqttConfig.getTopic() + "/cessna172/+/vsi/greyscale",
                mqttConfig.getTopic() + "/cessna172/+/dg/greyscale",
                mqttConfig.getTopic() + "/cessna172/+/alt/greyscale"
        };
    }
}
