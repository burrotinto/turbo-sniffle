package de.burrotinto.turboSniffle.mqtt;


import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
@Component
@RequiredArgsConstructor
public class MQTTClient implements InitializingBean {

    @NonNull
    private MQTTConfig mqttConfig;
    @NonNull
    private List<MQTTListener> listener;
    @NonNull
    private Gson gson;

    private Mqtt5AsyncClient client = null;

    private static final ExecutorService executorService = Executors.newWorkStealingPool();

    @Override
    public void afterPropertiesSet() throws Exception {

        client = MqttClient.builder()
                .useMqttVersion5()
                .identifier(mqttConfig.getClientID() + "_" + UUID.randomUUID())
                .serverHost(mqttConfig.getBroker())
                .serverPort(mqttConfig.getPort())
                .automaticReconnectWithDefaultConfig()
                .simpleAuth()
                .username(mqttConfig.getUsername())
                .password(mqttConfig.getPassword().getBytes(StandardCharsets.UTF_8))
                .applySimpleAuth()
                .buildAsync();

        client.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        // Handle connection failure
                    } else {
                        // Setup subscribes or start publishing
                    }
                });

        listener.forEach(listener -> {
            listener.setMqttClient(this);
            Arrays.stream(listener.getSubscribeTopic()).forEach(s -> {
                        client.subscribeWith()
                                .topicFilter(s)
                                .callback(mqtt5Publish -> {
                                    executorService.submit(() -> listener.newMessage(mqtt5Publish));
                                })
                                .send();
                    }
            );
        });


    }


    public void publish(String baseTopic, GaugeJSON json) {
        publish(baseTopic, gson.toJson(json));

        for (int i = 0; i < json.getPointer().length; i++) {
            publish(baseTopic + "/pointer_" + i, String.valueOf(json.getPointer()[i]));
        }
        publish(baseTopic + "/value", String.valueOf(json.value));
        publish(baseTopic + "/detected", json.detected);
        publish(baseTopic + "/gauge", json.gauge);
        publish(baseTopic + "/idealisiert", json.idealisierteDarstellung);

    }

    public void publish(String topic, String payload) {
        if (payload.length() > 64) {
            log.info("SENDING : TOPIC: " + topic + " | PAYLOAD: " + payload.substring(0, 64) + "...");
        } else {
            log.info("SENDING : TOPIC: " + topic + " | PAYLOAD: " + payload);
        }
        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.fromCode(mqttConfig.getQos()))
                .send();
    }

    public void publish(String topic, G1000JSON json) {
        publish(topic, gson.toJson(json));

        publish(topic + "/asi", String.valueOf(json.asi));
        publish(topic + "/dg", String.valueOf(json.dg));
        publish(topic + "/vsi", String.valueOf(json.vsi));
        publish(topic + "/alt", String.valueOf(json.alt));

        //        FileWriter fileWriter = new FileWriter("data/" + baseTopic.hashCode() + ".json");
//        PrintWriter printWriter = new PrintWriter(fileWriter);
//        printWriter.print(gson.toJson(json));
//        printWriter.close();
    }
}
