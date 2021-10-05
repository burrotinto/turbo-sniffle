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

public class MQTTSixpackSENDER {
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

        List<Path> files = listFiles(Paths.get("data/example/sixpack"));

        for (int i = 0; i < files.size(); i++) {
            String t = "turboSniffle/cessna172/" + i + "/XXX/greyscale";

            //Airspeed
            client.publishWith()
                    .topic(t.replace("XXX", "asi"))
                    .payload(MatToMessageString.erzeugeStringDarstellung(Imgcodecs.imread(files.get(i).toString(), Imgcodecs.IMREAD_GRAYSCALE).submat(50, 375, 150, 475)).getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();

            //ALT
            client.publishWith()
                    .topic(t.replace("XXX", "alt"))
                    .payload(MatToMessageString.erzeugeStringDarstellung(Imgcodecs.imread(files.get(i).toString(), Imgcodecs.IMREAD_GRAYSCALE).submat(50, 375, 750, 1075)).getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();

            //VSI
            client.publishWith()
                    .topic(t.replace("XXX", "vsi"))
                    .payload(MatToMessageString.erzeugeStringDarstellung(Imgcodecs.imread(files.get(i).toString(), Imgcodecs.IMREAD_GRAYSCALE).submat(350, 675, 750, 1075)).getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();

            //dg
            client.publishWith()
                    .topic(t.replace("XXX", "dg"))
                    .payload(MatToMessageString.erzeugeStringDarstellung(Imgcodecs.imread(files.get(i).toString(), Imgcodecs.IMREAD_GRAYSCALE).submat(350, 675, 300, 750)).getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();
            System.out.println(t);
            Thread.sleep(5000);
        }
    }
}
