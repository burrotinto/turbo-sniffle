package de.burrotinto.turboSniffle.mqtt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MQTTConfig {
    private String broker;
    private int port;
    private String protokoll ;
    private int qos;
    private String clientID;
    private String username;
    private String password;

    private String baseTopic;
}
