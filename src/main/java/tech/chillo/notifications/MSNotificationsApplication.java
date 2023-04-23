package tech.chillo.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MSNotificationsApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MSNotificationsApplication.class, args);
    }

}
