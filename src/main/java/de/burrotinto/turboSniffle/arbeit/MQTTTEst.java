package de.burrotinto.turboSniffle.arbeit;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import de.burrotinto.turboSniffle.mqtt.MatToMessageString;
import lombok.SneakyThrows;
import lombok.val;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static de.burrotinto.turboSniffle.arbeit.OnePointerErkennungBilder.listFiles;

public class MQTTTEst {
    @SneakyThrows
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        val client = MqttClient.builder()
                .useMqttVersion5()
                .identifier("TURBOSNIFFLE" + System.currentTimeMillis())
//                .serverHost("broker.hivemq.com")
                .serverHost("burrotinto.de")
                .serverPort(1883)
//                .automaticReconnectWithDefaultConfig()
                .buildAsync();

        client.connect();
        Thread.sleep(1000);

//        client.subscribeWith()
//                .topicFilter("turboSniffle/+/+/+/detected")
//                .callback(mqtt5Publish -> {
//                    HighGui.imshow(mqtt5Publish.getTopic().toString(), MatToMessageString.generateMatFromString(mqtt5Publish.getPayloadAsBytes()));
//                    HighGui.waitKey(1);
//                })
//                .send();

        new Thread(() -> {
            Mat g1000 = Imgcodecs.imread("data/ae/G1000/GarminG1000.png", Imgcodecs.IMREAD_GRAYSCALE);


            String topic = "turboSniffle/cessna172/X-PLANE/g1000/greyscale";
            client.publishWith()
                    .topic(topic)
                    .payload(MatToMessageString.generateMessage(g1000).getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send();
        }).start();
//
//

        new Thread(() ->{
            try {
                listFiles(Paths.get("data/example/gauge")).forEach(path -> {
                    String t = "turboSniffle/roundGauge/onePointer/" + path.toString() + "/greyscale";
                    System.out.println(t);
                    client.publishWith()
                            .topic(t)
                            .payload(MatToMessageString.generateMessage(Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_GRAYSCALE)).getBytes(StandardCharsets.UTF_8))
                            .qos(MqttQos.EXACTLY_ONCE)
                            .send();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } ).start();

//
        //2 Pointer
//        new Thread(() -> {
//            while (true) {
//                try {
//                    listFiles(Paths.get("data/example/gauge2Pointer")).forEach(path -> {
//                        String t = "turboSniffle/roundGauge/twoPointer/aaaaa/12/greyscale";
//
//                        client.publishWith()
//                                .topic(t)
//                                .payload(MatToMessageString.generateMessage(Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_GRAYSCALE)).getBytes(StandardCharsets.UTF_8))
////                                .qos(MqttQos.EXACTLY_ONCE)
//                                .send();
//                        System.out.println(t);
////                        try {
////                            Thread.sleep(1000);
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();


//        //ALTIMETER
//        new Thread(() ->{
//            try {
//                listFiles(Paths.get("data/example/sixpack")).parallelStream().forEach(path -> {
//                    String t = "turboSniffle/cessna172/X-PLANE/alt/greyscale";
//                    System.out.println(t);
//                    client.publishWith()
//                            .topic(t)
//                            .payload(MatToMessageString.generateMessage(Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_GRAYSCALE).submat(50, 375, 750, 1075)).getBytes(StandardCharsets.UTF_8))
//                            .qos(MqttQos.EXACTLY_ONCE)
//                            .send();
//
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } ).start();
    }

    public void publish(String topic, String payload) {


    }
}
