package de.burrotinto.turboSniffle;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TurboSniffleApplication {


    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        var ctx = new SpringApplicationBuilder(TurboSniffleApplication.class)
                .headless(false).run(args);


    }

}
