package tech.chillo.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@SpringBootTest
class MSNotificationsApplicationTests {

    @Test
    void contextLoads() {
    }

}
