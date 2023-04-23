package tech.chillo.notifications.service.sms;

import com.google.common.base.Strings;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.SmsSubmissionResponseMessage;
import com.vonage.client.sms.messages.TextMessage;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.entity.NotificationTemplate;
import tech.chillo.notifications.entity.Recipient;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.repository.NotificationTemplateRepository;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VonageSMSService {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final VonageClient client;

    public VonageSMSService(
            @Value("${providers.vonage.api_key}") final String vonageAccountId,
            @Value("${providers.vonage.api_secret}") final String vonageAccountSecret,
            final NotificationTemplateRepository notificationTemplateRepository
    ) {
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.client = VonageClient.builder().apiKey(vonageAccountId).apiSecret(vonageAccountSecret).build();
    }

    @Async
    public List<NotificationStatus> send(final Notification notification) {
        return notification.getContacts().parallelStream().map((Recipient to) -> {
            try {
                notification.setContacts(Set.of(to));
                Map<String, Object> params = notification.getParams();

                if (params == null) {
                    params = new HashMap<>();
                }
                final Object message = params.get("message");
                String messageAsString = null;
                if (message != null) {
                    messageAsString = message.toString();
                    final BeanInfo beanInfo = Introspector.getBeanInfo(Recipient.class);
                    for (final PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
                        final String propertyName = propertyDesc.getName();
                        final Object value = propertyDesc.getReadMethod().invoke(to);
                        if (!Strings.isNullOrEmpty(String.valueOf(value)) && value instanceof String) {
                            if (Objects.equals(propertyName, "phone")) {
                                messageAsString = messageAsString.replace(String.format("%s%s%s", "{{", propertyName, "}}"), String.format("00%s%s", to.getPhoneIndex(), to.getPhone()));
                            } else {
                                messageAsString = messageAsString.replace(String.format("%s%s%s", "{{", propertyName, "}}"), (CharSequence) value);
                            }
                        }
                    }
                }
                params.put("message", messageAsString);
                params.put("firstName", to.getFirstName());
                params.put("lastName", to.getLastName());
                params.put("civility", to.getCivility());
                params.put("email", to.getEmail());
                params.put("phone", to.getPhone());
                params.put("phoneIndex", to.getPhoneIndex());
                String messageToSend = notification.getMessage();

                if (!Strings.isNullOrEmpty(notification.getTemplate())) {
                    NotificationTemplate notificationTemplate = this.notificationTemplateRepository
                            .findByApplicationAndName(notification.getApplication(), notification.getTemplate())
                            .orElseThrow(() -> new IllegalArgumentException(String.format("Aucun template %s n'existe pour %s", notification.getTemplate(), notification.getApplication())));
                    //final String template = this.textTemplateEngine.process(notificationTemplate.getContent(), context);
                    messageToSend = this.processTemplate(params, notificationTemplate.getContent());
                } else {
                    messageToSend = messageToSend.replaceAll(Pattern.quote("{{"), Matcher.quoteReplacement("${"))
                            .replaceAll(Pattern.quote("}}"), Matcher.quoteReplacement("}"));

                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(messageToSend);
                    messageToSend = this.processTemplate(params, messageToSend);
                }


                final NotificationStatus notificationStatus = new NotificationStatus();
                final Object eventId = notification.getEventId();
                notificationStatus.setEventId((String) eventId);
                notificationStatus.setUserId(to.getId());
                notificationStatus.setChannel(NotificationType.SMS);

                String phoneNumber = String.format("+%s%s", to.getPhoneIndex(), to.getPhone());
                phoneNumber = phoneNumber.replace("+", "");

                TextMessage vonageMessage = new TextMessage("Vonage APIs",
                        "33761705745", //phoneNumber
                        messageToSend
                );
                SmsSubmissionResponse response = this.client.getSmsClient().submitMessage(vonageMessage);
                SmsSubmissionResponseMessage submissionResponseMessage = response.getMessages().get(0);
                notificationStatus.setProviderNotificationId(submissionResponseMessage.getId());
                notificationStatus.setStatus(submissionResponseMessage.getStatus().name());
                notificationStatus.setProvider("VONAGE");
                return notificationStatus;
            } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    private String processTemplate(Map model, String template) {
        try {
            Template t = new Template("TemplateFromDBName", template, null);
            Writer out = new StringWriter();
            t.process(model, out);
            return out.toString();

        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
