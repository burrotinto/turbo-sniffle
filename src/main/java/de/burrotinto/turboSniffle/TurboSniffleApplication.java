package de.burrotinto.turboSniffle;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TurboSniffleApplication {

    static {
        nu.pattern.OpenCV.loadLocally();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
//        SpringApplication.run(PopeyeApplication.class, args);


            var ctx = new SpringApplicationBuilder(TurboSniffleApplication.class)
                    .headless(false).run(args);

//            EventQueue.invokeLater(() -> {
//
//                var ex = ctx.getBean(SwingApp.class);
//                ex.setVisible(true);
//            });

    }

}
