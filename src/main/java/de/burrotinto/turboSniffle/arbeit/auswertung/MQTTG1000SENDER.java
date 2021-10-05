package de.burrotinto.turboSniffle.arbeit.auswertung;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import de.burrotinto.turboSniffle.arbeit.G1000;
import de.burrotinto.turboSniffle.gauge.GarminG1000;
import de.burrotinto.turboSniffle.mqtt.MatToMessageString;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static de.burrotinto.turboSniffle.arbeit.OnePointerErkennungBilder.listFiles;

public class MQTTG1000SENDER {
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

        List<Path> files = listFiles(Paths.get("data/example/g1000Auswertung"));

        for (int i = 0; i < files.size(); i++) {
            String t = "turboSniffle/cessna172/"+ i +"/g1000/greyscale";


                    ArrayList<Point> corner = new ArrayList<>();
        corner.add(new Point(329, 202));
        corner.add(new Point(966, 202));
        corner.add(new Point(954, 661));
        corner.add(new Point(343, 662));

            client.publishWith()
                    .topic(t)
                    .payload(MatToMessageString.erzeugeStringDarstellung( G1000.transformieren(Imgcodecs.imread(files.get(i).toString(), Imgcodecs.IMREAD_GRAYSCALE), corner, new Size(1024,768)))
                            .getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();

            System.out.println(t);
            Thread.sleep(5000);
        }
    }
}
