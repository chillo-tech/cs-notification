package tech.chillo.notifications.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitErrorHandler implements RabbitListenerErrorHandler {
    @Override
    public Object handleError(final Message message, final org.springframework.messaging.Message<?> message1, final ListenerExecutionFailedException e) throws Exception {
        e.printStackTrace();
        log.info(message.toString(), e);
        return null;
    }
}
