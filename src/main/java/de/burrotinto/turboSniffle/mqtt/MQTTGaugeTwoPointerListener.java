package de.burrotinto.turboSniffle.mqtt;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
import de.burrotinto.turboSniffle.meters.gauge.ValueGauge;
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

//        try {
//            ValueGauge gauge10 = GaugeFactory.getTwoPointerValueGauge(GaugeFactory.getGaugeFromMat(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes())), 10);
//            mqttClient.publish(topic + "/pointerAngleSmall", String.valueOf(gauge10.getPointerAngel()[0]));
//            mqttClient.publish(topic + "/pointerAngleTall", String.valueOf(gauge10.getPointerAngel()[1]));
//            mqttClient.publish(topic + "/10/value", String.valueOf(gauge10.getValue()));
//            mqttClient.publish(topic + "/10/detected", MatToMessageString.generateMessage(gauge10.getDrawing(gauge10.getSource())));
//
//            ValueGauge gauge12 = GaugeFactory.getTwoPointerValueGauge(GaugeFactory.getGaugeFromMat(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes())), 12);
//            mqttClient.publish(topic + "/12/value", String.valueOf(gauge12.getValue()));
//            mqttClient.publish(topic + "/12/detected", MatToMessageString.generateMessage(gauge12.getDrawing(gauge12.getSource())));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            mqttClient.publish(topic + "/EXCEPTION", e.getLocalizedMessage());
//        }
        ValueGauge gauge = GaugeFactory.getTwoPointerValueGauge( GaugeFactory.getGauge(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes())), Integer.parseInt(publish.getTopic().getLevels().get(4)));

        GaugeJSON json = new GaugeJSON(gauge);
        json.src = new String(publish.getPayloadAsBytes(), "UTF-8");

        mqttClient.publish(topic,json);

    }

    @Override
    public String[] getSubscribeTopic() {
        return new String[]{mqttConfig.getTopic() + "/roundGauge/twoPointer/+/+/greyscale"};
    }
}
