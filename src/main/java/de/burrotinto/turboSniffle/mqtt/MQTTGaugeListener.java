//package de.burrotinto.turboSniffle.mqtt;
//
//import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
//import de.burrotinto.turboSniffle.meters.gauge.Gauge;
//import de.burrotinto.turboSniffle.meters.gauge.GaugeFactory;
//import de.burrotinto.turboSniffle.meters.gauge.NotGaugeWithPointerException;
//import de.burrotinto.turboSniffle.meters.gauge.ValueGauge;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//
//@Component
//@RequiredArgsConstructor
//public class MQTTGaugeListener extends MQTTListener {
//    @NonNull
//    private MQTTConfig mqttConfig;
//
//
//
//    @Override
//    public void newMessage(Mqtt5Publish publish) {
//        super.newMessage(publish);
//
//        String topic = publish.getTopic().toString().replace("/greyscale", "");
//        try {
//            Gauge gauge = GaugeFactory.getGauge(MatToMessageString.generateMatFromString(publish.getPayloadAsBytes()));
//            mqttClient.publish(topic + "/gauge", MatToMessageString.generateMessage(gauge.getSource()));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            mqttClient.publish(topic + "/EXCEPTION", e.getLocalizedMessage());
//        }
//
//
// }
//
//    @Override
//    public String[] getSubscribeTopic() {
//        return new String[]{mqttConfig.getTopic() + "/roundGauge/+/+/greyscale"};
//    }
//}
