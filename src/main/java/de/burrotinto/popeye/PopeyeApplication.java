package de.burrotinto.popeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.awt.*;

@SpringBootApplication
public class PopeyeApplication {


    public static void main(String[] args) {

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
