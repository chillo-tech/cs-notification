package tech.chillo.notifications.service;

import com.google.common.base.Strings;
import freemarker.template.Template;
import freemarker.template.TemplateException;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NotificationMapper {
    private final NotificationTemplateRepository notificationTemplateRepository;

    public NotificationMapper(NotificationTemplateRepository notificationTemplateRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    protected Map<String, Object> map(Notification notification, Recipient to) {
        try {

            // Paramètres transmis pour le message
            Map<String, Object> params = notification.getParams();

            if (params == null) {
                params = new HashMap<>();
            }
            final Object message = params.get("message");
            String messageAsString = null;

            // Informations de l'utilisateur dans le template
            if (message != null) {
                messageAsString = message.toString();
                final BeanInfo beanInfo = Introspector.getBeanInfo(Recipient.class);
                // Traitement de chaque propriété
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
            return Map.of("message", messageToSend, "params", params);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected NotificationStatus getNotificationStatus(Notification notification, String userId, NotificationType notificationType, String providerId, String status) {
        final NotificationStatus notificationStatus = new NotificationStatus();
        notificationStatus.setEventId(notification.getEventId());
        notificationStatus.setLocalNotificationId(notification.getId());
        notificationStatus.setUserId(userId);
        notificationStatus.setChannel(notificationType);
        notificationStatus.setProviderNotificationId(providerId);
        notificationStatus.setStatus(status);
        notificationStatus.setCreation(Instant.now());
        return notificationStatus;
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
