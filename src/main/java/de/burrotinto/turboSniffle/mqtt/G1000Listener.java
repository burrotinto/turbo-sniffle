package de.burrotinto.turboSniffle.mqtt;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import de.burrotinto.turboSniffle.gauge.GarminG1000;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Log
@Component
@RequiredArgsConstructor
public class G1000Listener extends MQTTListener {
    @NonNull
    private MQTTConfig mqttConfig;

    private HashMap<String, GarminG1000> g1000HashMap = new HashMap<>();


    public void newMessage(Mqtt5Publish publish) {
        super.newMessage(publish);

        String id = getID(publish.getTopic());
        GarminG1000 g1000 = new GarminG1000(MatToMessageString.erzeugeMatAusStringDarstellung(publish.getPayloadAsBytes()));
        g1000HashMap.put(id, g1000);

        String topic = publish.getTopic().toString().replace("/greyscale", "");
        G1000JSON json = new G1000JSON(g1000);
        mqttClient.publish(topic,json);

//        mqttClient.publish(topic + "/ocrOptimiert", MatToMessageString.generateMessage(g1000.getOCROptimiert()));
    }

    @Override
    public String[] getSubscribeTopic() {
        return new String[]{mqttConfig.getBaseTopic()+"/cessna172/+/g1000/greyscale"};
    }


}
