package de.burrotinto.turboSniffle.mqtt;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.burrotinto.turboSniffle.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.gauge.ValueGauge;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MQTTGaugeTwoPointerListener extends MQTTListener {
    @NonNull
    private MQTTConfig mqttConfig;


    @SneakyThrows
    @Override
    public void newMessage(Mqtt5Publish publish) {
        super.newMessage(publish);

        String topic = publish.getTopic().toString()
                .replace("/greyscale", "");

        ValueGauge gauge = GaugeFactory.getTwoPointerValueGauge(GaugeFactory.getGauge(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes())),
                Integer.parseInt(publish.getTopic().getLevels().get(publish.getTopic().getLevels().size() - 2)));

        GaugeJSON json = new GaugeJSON(gauge);

        mqttClient.publish(topic, json);

    }

    @Override
    public String[] getSubscribeTopic() {
        return new String[]{mqttConfig.getBaseTopic() + "/roundGauge/twoPointer/+/+/greyscale"};
    }
}
