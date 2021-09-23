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
public class MQTTGaugeOnePointerListener extends MQTTListener {
    @NonNull
    private MQTTConfig mqttConfig;


    @SneakyThrows
    @Override
    public void newMessage(Mqtt5Publish publish) {
        super.newMessage(publish);

        String topic = publish.getTopic().toString()
                .replace("/greyscale", "");

        ValueGauge gauge = GaugeFactory.getGaugeWithOnePointerAutoScale( GaugeFactory.getGauge(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes())));

        GaugeJSON json = new GaugeJSON(gauge);
        json.src = new String(publish.getPayloadAsBytes(), "UTF-8");

        mqttClient.publish(topic,json);
//        try {
//            ValueGauge gauge = GaugeFactory.getGaugeWithOnePointerAutoScale(GaugeFactory.getGaugeFromMat(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes())));
//
//            mqttClient.publish(topic + "/pointerAngle", String.valueOf(gauge.getPointerAngel()[0]));
//            mqttClient.publish(topic + "/value", String.valueOf(gauge.getValue()));
//            mqttClient.publish(topic + "/detected", MatToMessageString.generateMessage(gauge.getDrawing(gauge.getSource())));
//
//        } catch (NotGaugeWithPointerException e) {
//            e.printStackTrace();
//            mqttClient.publish(topic + "/EXCEPTION", e.getLocalizedMessage());
//        }


    }

    @Override
    public String[] getSubscribeTopic() {
        return new String[]{mqttConfig.getBaseTopic() + "/roundGauge/onePointer/+/greyscale"};
    }
}
