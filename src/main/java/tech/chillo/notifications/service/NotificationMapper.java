package tech.chillo.notifications.service;

import com.google.common.base.Strings;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class NotificationMapper {
    private final NotificationTemplateRepository notificationTemplateRepository;

    public NotificationMapper(final NotificationTemplateRepository notificationTemplateRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    protected Map<String, Object> map(final Notification notification, final Recipient to) {
        try {

            // Paramètres transmis pour le message
            Map<String, List<Object>> params = notification.getParams();

            if (params == null) {
                params = new HashMap<>();
            }

            final Map<String, List<Object>> othersParams = new HashMap<>();
            to.getOthers().forEach(other -> othersParams.put(other.getLabel().replaceAll("\\s+", ""), List.of(other.getValue())));
            for (final String otherParamKey : othersParams.keySet()) {
                params.put(otherParamKey, othersParams.get(otherParamKey));
            }

            final Object message = params.get("message");
            String messageAsString = "";

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
            params.put("message", List.of(messageAsString));
            params.put("firstName", List.of(to.getFirstName()));
            params.put("lastName", List.of(to.getLastName()));
            params.put("civility", List.of(to.getCivility()));
            params.put("email", List.of(to.getEmail()));
            if (!Strings.isNullOrEmpty(to.getPhone())) {
                params.put("phone", List.of(to.getPhone()));
            }
            if (!Strings.isNullOrEmpty(to.getPhoneIndex())) {
                params.put("phoneIndex", List.of(to.getPhoneIndex()));
            }
            String messageToSend = notification.getMessage();

            if (!Strings.isNullOrEmpty(notification.getTemplate())) {
                final NotificationTemplate notificationTemplate = this.notificationTemplateRepository
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
        } catch (final IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected NotificationStatus getNotificationStatus(final Notification notification, final String userId, final NotificationType notificationType, final String providerId, final String status) {
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

    protected String processTemplate(final Map<String, List<Object>> model, final String template) {
        final Map<String, Object> oneItemMap = new HashMap<>();
        final Map<String, List<Object>> moreThanOneItemMap = new HashMap<>();
        model.keySet()
                .forEach(key -> {
                    if (model.get(key).size() == 1) {
                        oneItemMap.put(key, model.get(key).get(0));
                    } else {
                        oneItemMap.put(key, "${" + key + "}");
                    }
                });
        model.keySet()
                .stream()
                .filter(key -> model.get(key).size() > 1)
                .forEach(key -> moreThanOneItemMap.put(key, model.get(key)));

        final String[] parsedTemplate = {this.processTemplateWithValues(oneItemMap, template)};
        moreThanOneItemMap.keySet().forEach(key -> {
            final List<Object> values = moreThanOneItemMap.get(key);
            for (final Object replacement : values) {
                parsedTemplate[0] = parsedTemplate[0].replaceFirst(String.format("%s%s%s", "\\$\\{", key, "}"), (String) replacement);
            }
        });
        return parsedTemplate[0];
    }

    private String processTemplateWithValues(final Map<String, Object> model, final String template) {

        try {
            String templateHoHandle = template;
            final Matcher m = Pattern.compile("\\$\\{(.*?)}").matcher(template);
            while (m.find()) {
                final String initialVariable = m.group(1);
                final String finalVariable = initialVariable.replaceAll("\\s+", "");
                templateHoHandle = templateHoHandle.replaceAll(initialVariable, finalVariable);
            }

            final Template t = new Template("TemplateFromDBName", templateHoHandle, null);
            final Writer out = new StringWriter();
            t.process(model, out);
            return out.toString();

        } catch (final TemplateException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
