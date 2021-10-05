package de.burrotinto.turboSniffle.arbeit.auswertung;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import de.burrotinto.turboSniffle.mqtt.MatToMessageString;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static de.burrotinto.turboSniffle.arbeit.OnePointerErkennungBilder.listFiles;

public class AuswertungDurchMQTTZweiZeigerSENDER {
    @SneakyThrows
    public static void main(String[] args) {
        //OpenCV laden
        nu.pattern.OpenCV.loadLocally();

        val client = MqttClient.builder()
                .useMqttVersion5()
                .identifier("TURBOSNIFFLE" + System.currentTimeMillis())
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildAsync();

        client.connect();
        Thread.sleep(1000);

        List<Path> files = listFiles(Paths.get("data/example/gauge2Pointer"));

        for (int i = 0; i < files.size(); i++) {
            String t = "turboSniffle/roundGauge/twoPointer/" + i + "/12/greyscale";
            client.publishWith()
                    .topic(t)
                    .payload(MatToMessageString.erzeugeStringDarstellung(Imgcodecs.imread(files.get(i).toString(), Imgcodecs.IMREAD_GRAYSCALE)).getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();
            System.out.println(t);

            Thread.sleep(1000);
        }
    }
}
