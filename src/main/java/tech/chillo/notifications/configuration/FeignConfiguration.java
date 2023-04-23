package tech.chillo.notifications.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignConfiguration implements RequestInterceptor {

    private final String token;

    public FeignConfiguration(@Value("${providers.whatsapp.token}") final String token) {
        this.token = token;
    }

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        requestTemplate.header("Accept", "application/json");
        requestTemplate.header("Authorization", "Bearer " + this.token);
    }
}
