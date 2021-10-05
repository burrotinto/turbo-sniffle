package de.burrotinto.turboSniffle.arbeit.auswertung;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttClient;
import de.burrotinto.turboSniffle.arbeit.G1000;
import de.burrotinto.turboSniffle.cv.Helper;
import de.burrotinto.turboSniffle.gauge.GarminG1000;
import de.burrotinto.turboSniffle.mqtt.G1000JSON;
import de.burrotinto.turboSniffle.mqtt.GaugeJSON;
import de.burrotinto.turboSniffle.mqtt.MatToMessageString;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.math3.util.Precision;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static de.burrotinto.turboSniffle.arbeit.OnePointerErkennungBilder.listFiles;


public class Empfaenger {

    @SneakyThrows
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        val client = MqttClient.builder()
                .useMqttVersion5()
                .identifier("TURBOSNIFFLE" + System.currentTimeMillis())
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildAsync();

        client.connect();


        Gson gson = new Gson();


        //EINZEIGER Grey
        //  "turboSniffle/roundGauge/onePointer/" + i + "/greyscale"
        client.subscribeWith()
                .topicFilter("turboSniffle/roundGauge/onePointer/+/greyscale")
                .callback(mqtt5Publish -> {
                    System.out.println(mqtt5Publish.getTopic().toString());
                    Imgcodecs.imwrite("data/out/auswertung/onePointer/"
                            + mqtt5Publish.getTopic().getLevels().get(3)
                            + "_" + mqtt5Publish.getTopic().getLevels().get(4)
                            + ".png", MatToMessageString.erzeugeMatAusStringDarstellung(mqtt5Publish.getPayloadAsBytes()));
                })
                .send();

        //EINZEIGER JSON
        //  "turboSniffle/roundGauge/onePointer/" + i + "/greyscale"
        client.subscribeWith()
                .topicFilter("turboSniffle/roundGauge/onePointer/+")
                .callback(mqtt5Publish -> {
                    System.out.println(mqtt5Publish.getTopic().toString());
                    try {
                        GaugeJSON json = gson.fromJson(new String(mqtt5Publish.getPayloadAsBytes(), "UTF-8"), GaugeJSON.class);
                        System.out.println(mqtt5Publish.getTopic() + " VALUE:" + json.getValue());
                        System.out.println(mqtt5Publish.getTopic() + " ANGLE:" + json.getPointer()[0]);

                        Imgcodecs.imwrite("data/out/auswertung/onePointer/"
                                        + mqtt5Publish.getTopic().getLevels().get(3)
                                        + "_ideal.png",
                                MatToMessageString.erzeugeMatAusStringDarstellung(json.getIdealisierteDarstellung()));

                        Imgcodecs.imwrite("data/out/auswertung/onePointer/"
                                        + mqtt5Publish.getTopic().getLevels().get(3)
                                        + "_detected.png",
                                MatToMessageString.erzeugeMatAusStringDarstellung(json.getDetected()));
                        try {
                            FileWriter myWriter = new FileWriter("data/out/auswertung/onePointer/" + mqtt5Publish.getTopic().getLevels().get(3) + ".txt");
                            myWriter.write("Value: " + json.getValue() + "\nAngle: " + json.getPointer()[0]);
                            myWriter.close();
                        } catch (IOException e) {
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                })
                .send();


        //EINZEIGER Grey
        //  "turboSniffle/roundGauge/onePointer/" + i + "/greyscale"
        client.subscribeWith()
                .topicFilter("turboSniffle/roundGauge/twoPointer/+/+/greyscale")
                .callback(mqtt5Publish -> {
                    System.out.println(mqtt5Publish.getTopic().toString());
                    Imgcodecs.imwrite("data/out/auswertung/twoPointer/"
                            + mqtt5Publish.getTopic().getLevels().get(3)
                            + ".png", MatToMessageString.erzeugeMatAusStringDarstellung(mqtt5Publish.getPayloadAsBytes()));
                })
                .send();

        client.subscribeWith()
                .topicFilter("turboSniffle/roundGauge/twoPointer/+/+")
                .callback(mqtt5Publish -> {
                    System.out.println(mqtt5Publish.getTopic().toString());
                    try {
                        GaugeJSON json = gson.fromJson(new String(mqtt5Publish.getPayloadAsBytes(), "UTF-8"), GaugeJSON.class);
                        System.out.println(mqtt5Publish.getTopic() + " VALUE:" + json.getValue());
                        System.out.println(mqtt5Publish.getTopic() + " ANGLE:" + json.getPointer()[0]);

                        Imgcodecs.imwrite("data/out/auswertung/twoPointer/"
                                        + mqtt5Publish.getTopic().getLevels().get(3)
                                        + "_ideal.png",
                                MatToMessageString.erzeugeMatAusStringDarstellung(json.getIdealisierteDarstellung()));

                        Imgcodecs.imwrite("data/out/auswertung/twoPointer/"
                                        + mqtt5Publish.getTopic().getLevels().get(3)
                                        + "_detected.png",
                                MatToMessageString.erzeugeMatAusStringDarstellung(json.getDetected()));
                        try {
                            FileWriter myWriter = new FileWriter("data/out/auswertung/twoPointer/" + mqtt5Publish.getTopic().getLevels().get(3) + ".txt");
                            myWriter.write("Value: " + json.getValue() + "\nAngle: " + json.getPointer()[0]);
                            myWriter.close();
                        } catch (IOException e) {
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                })
                .send();


        client.subscribeWith()
                .topicFilter("turboSniffle/cessna172/+/+/greyscale")
                .callback(mqtt5Publish -> {
                    System.out.println(mqtt5Publish.getTopic().toString());
                    Imgcodecs.imwrite("data/out/auswertung/sixpack/"
                            + mqtt5Publish.getTopic().getLevels().get(3)
                            + "_" + mqtt5Publish.getTopic().getLevels().get(4)
                            + ".png", MatToMessageString.erzeugeMatAusStringDarstellung(mqtt5Publish.getPayloadAsBytes()));
                })
                .send();





        List<Path> files = listFiles(Paths.get("data/example/g1000Auswertung"));

        client.subscribeWith()
                .topicFilter("turboSniffle/cessna172/+/+")
                .callback(mqtt5Publish -> {
                    System.out.println(mqtt5Publish.getTopic().toString());
                    if (mqtt5Publish.getTopic().getLevels().get(3).equals("g1000")) {

                        try {
                            G1000JSON json = gson.fromJson(new String(mqtt5Publish.getPayloadAsBytes(), "UTF-8"), G1000JSON.class);


                            ArrayList<Point> corner = new ArrayList<>();
                            corner.add(new Point(329, 202));
                            corner.add(new Point(966, 202));
                            corner.add(new Point(954, 661));
                            corner.add(new Point(343, 662));

                            Mat g1000 = G1000.transformieren(Imgcodecs.imread(files.get(Integer.parseInt(mqtt5Publish.getTopic().getLevels().get(2))).toString(), Imgcodecs.IMREAD_GRAYSCALE), corner, GarminG1000.SIZE);

                            Mat draw = Mat.zeros(GarminG1000.SIZE, g1000.type());
                            Imgproc.resize(g1000,draw,new Size(1024,768));

                            Imgproc.rectangle(draw, new Point(0, 0), new
                                    Point(400, 220), Helper.WHITE, -1);


                            Imgproc.putText(draw, "ASI= " + Precision.round(json.getAsi(),2), new Point(0, 50), 0, 1.0, Helper.BLACK);
                            Imgproc.putText(draw, "ALT= " + Precision.round(json.getAlt(),2), new Point(0, 100), 0, 1.0, Helper.BLACK);
                            Imgproc.putText(draw, "VSI= " + json.getVsi(), new Point(0, 150), 0, 1.0, Helper.BLACK);
                            Imgproc.putText(draw, "DG= " + json.getDg(), new Point(0, 200), 0, 1.0, Helper.BLACK);


                            Imgcodecs.imwrite("data/out/auswertung/g1000/"+mqtt5Publish.getTopic().getLevels().get(2)+".png",draw);
                            HighGui.imshow("",draw);
                            HighGui.waitKey(1);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            GaugeJSON json = gson.fromJson(new String(mqtt5Publish.getPayloadAsBytes(), "UTF-8"), GaugeJSON.class);
                            System.out.println(mqtt5Publish.getTopic() + " VALUE:" + json.getValue());
                            System.out.println(mqtt5Publish.getTopic() + " ANGLE:" + json.getPointer()[0]);

                            Imgcodecs.imwrite("data/out/auswertung/sixpack/"
                                            + mqtt5Publish.getTopic().getLevels().get(3)
                                            + mqtt5Publish.getTopic().getLevels().get(2)
                                            + "_ideal.png",
                                    MatToMessageString.erzeugeMatAusStringDarstellung(json.getIdealisierteDarstellung()));

                            Imgcodecs.imwrite("data/out/auswertung/sixpack/"
                                            + mqtt5Publish.getTopic().getLevels().get(3)
                                            + mqtt5Publish.getTopic().getLevels().get(2)
                                            + "_detected.png",
                                    MatToMessageString.erzeugeMatAusStringDarstellung(json.getDetected()));
                            try {
                                FileWriter myWriter = new FileWriter("data/out/auswertung/sixpack/" + mqtt5Publish.getTopic().getLevels().get(3) + mqtt5Publish.getTopic().getLevels().get(2) + ".txt");
                                myWriter.write("Value: " + json.getValue() + "\nAngle: " + json.getPointer()[0]);
                                myWriter.close();
                            } catch (IOException e) {
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .send();
    }


}
