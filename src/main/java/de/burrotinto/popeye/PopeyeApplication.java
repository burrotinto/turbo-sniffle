package de.burrotinto.popeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.awt.*;

@SpringBootApplication
public class PopeyeApplication {

    static {
        nu.pattern.OpenCV.loadLocally();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
//        SpringApplication.run(PopeyeApplication.class, args);


            var ctx = new SpringApplicationBuilder(PopeyeApplication.class)
                    .headless(false).run(args);

//            EventQueue.invokeLater(() -> {
//
//                var ex = ctx.getBean(SwingApp.class);
//                ex.setVisible(true);
//            });

    }

}
