package tech.chillo.notifications.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class WhatsAppFeignConfiguration implements RequestInterceptor {

    private final String token;

    public WhatsAppFeignConfiguration(
            @Value("${providers.whatsapp.token}") final String token) {
        this.token = token;
    }

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        requestTemplate.header("Accept", APPLICATION_JSON_VALUE);
        if (requestTemplate.feignTarget().name().equalsIgnoreCase("whatsappmessages")) {
            requestTemplate.header("Authorization", "Bearer " + this.token);
        }
        //requestTemplate.header("Authorization", "Bearer " + this.token);
    }
}
