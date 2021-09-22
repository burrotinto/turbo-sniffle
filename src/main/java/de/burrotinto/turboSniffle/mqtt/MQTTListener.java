package de.burrotinto.turboSniffle.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
abstract public class MQTTListener {

    @Setter
    protected MQTTClient mqttClient = null;

    @SneakyThrows
    public void newMessage(Mqtt5Publish publish) {
        if(new String(publish.getPayloadAsBytes(), "UTF-8").length() < 128) {
            log.info(publish.getTopic() + " : " + new String(publish.getPayloadAsBytes(), "UTF-8"));
        } else {
            log.info(publish.getTopic() + " : " + new String(publish.getPayloadAsBytes(), "UTF-8").length() +" Chars");
        }
    }

    abstract public String[] getSubscribeTopic();


    public static String getID(MqttTopic topic) {
        return topic.getLevels().get(2);
    }
}
