package tech.chillo.notifications.service.whatsapp;

import com.google.common.base.Strings;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.entity.NotificationTemplate;
import tech.chillo.notifications.entity.Recipient;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.repository.NotificationTemplateRepository;
import tech.chillo.notifications.service.whatsapp.dto.Language;
import tech.chillo.notifications.service.whatsapp.dto.TextMessage;
import tech.chillo.notifications.service.whatsapp.dto.WhatsappTemplate;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class WhatsappService {
    TextMessageService textMessageService;
    private final NotificationTemplateRepository notificationTemplateRepository;

    /*
        public void send(final Notification notification) {
            notification.getContacts().parallelStream().forEach((Recipient to) -> {

                final WhatsappTemplate template = new WhatsappTemplate();
                template.setName("hello_world");
                //template.setNamespace("newcourseevent");

                template.setLanguage(new Language("en_US"));

                final Component component = new Component();
                component.setType("body");
                final List<Parameter> parameters = notification.getParams().keySet()
                        .parallelStream().map(param -> new Parameter("text", notification.getParams().get(param), null))
                        .collect(Collectors.toList());
                parameters.add(new Parameter("text", String.format("%s %s", to.getFirstName(), to.getLastName().toUpperCase()), null));
                component.setParameters(parameters);

                final TextMessage textMessage = new TextMessage();
                textMessage.setTemplate(template);
                textMessage.setMessaging_product("whatsapp");
                textMessage.setType("template");
                textMessage.setTo(to.getPhone());
                //this.textMessageService.message(textMessage);
            });
        }
    */
    @Async
    public List<NotificationStatus> send(final Notification notification) {
        return notification.getContacts().parallelStream().map((Recipient to) -> {
            try {
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

                    messageToSend = this.processTemplate(params, messageToSend);
                }


                final NotificationStatus notificationStatus = new NotificationStatus();
                final Object eventId = notification.getEventId();
                notificationStatus.setEventId((String) eventId);
                notificationStatus.setUserId(to.getId());
                notificationStatus.setChannel(NotificationType.WHATSAPP);

                String phoneNumber = String.format("+%s%s", to.getPhoneIndex(), to.getPhone());


                final WhatsappTemplate template = new WhatsappTemplate();
                template.setName("hello_world");
                //template.setNamespace("newcourseevent");

                template.setLanguage(new Language("en_US"));
                final TextMessage textMessage = new TextMessage();
                textMessage.setTemplate(template);
                textMessage.setMessaging_product("whatsapp");
                textMessage.setType("template");
                textMessage.setTo(phoneNumber);

                this.textMessageService.message(textMessage);

                //notificationStatus.setProviderNotificationId(createdMessage.getSid());
                notificationStatus.setStatus("SENT");
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
