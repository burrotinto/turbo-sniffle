package de.burrotinto.turboSniffle.mqtt;


import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
                .identifier(mqttConfig.getClientID())
                .serverHost(mqttConfig.getServer())
                .serverPort(mqttConfig.getPort())
                .automaticReconnectWithDefaultConfig()
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
    }

    public void publish(String topic, String payload) {
        log.info("TOPIC: "+ topic+ " | PAYLOAD: " + payload.length());
        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
    }
}
