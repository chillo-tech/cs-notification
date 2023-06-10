package tech.chillo.notifications.service.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.entity.Recipient;
import tech.chillo.notifications.entity.TemplateStatus;
import tech.chillo.notifications.entity.template.Template;
import tech.chillo.notifications.entity.template.TemplateComponent;
import tech.chillo.notifications.entity.template.TemplateExample;
import tech.chillo.notifications.entity.template.WhatsAppTemplate;
import tech.chillo.notifications.repository.NotificationTemplateRepository;
import tech.chillo.notifications.repository.TemplateRepository;
import tech.chillo.notifications.repository.TemplateStatusRepository;
import tech.chillo.notifications.service.NotificationMapper;
import tech.chillo.notifications.service.whatsapp.dto.Component;
import tech.chillo.notifications.service.whatsapp.dto.Language;
import tech.chillo.notifications.service.whatsapp.dto.Parameter;
import tech.chillo.notifications.service.whatsapp.dto.Text;
import tech.chillo.notifications.service.whatsapp.dto.TextMessage;
import tech.chillo.notifications.service.whatsapp.dto.WhatsAppResponse;
import tech.chillo.notifications.service.whatsapp.dto.WhatsappTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.chillo.notifications.data.ApplicationData.FOOTER_TEXT;
import static tech.chillo.notifications.enums.NotificationType.WHATSAPP;
import static tech.chillo.notifications.enums.TemplateCategory.UTILITY;
import static tech.chillo.notifications.enums.TemplateComponentType.BODY;
import static tech.chillo.notifications.enums.TemplateComponentType.FOOTER;

@Slf4j
@Service
public class WhatsappService extends NotificationMapper {

    private final TemplateRepository templateRepository;
    private final TemplateStatusRepository templateStatusRepository;
    private final String recipient;
    private final TextMessageService textMessageService;
    private final TemplateMessageService templateMessageService;
    private final NotificationTemplateRepository notificationTemplateRepository;

    public WhatsappService(
            TemplateRepository templateRepository, @Value("${application.recipient.sms:#{null}}") final String recipient,
            TemplateStatusRepository templateStatusRepository,
            TextMessageService textMessageService,
            TemplateMessageService templateMessageService,
            NotificationTemplateRepository notificationTemplateRepository
    ) {
        super(notificationTemplateRepository);
        this.templateRepository = templateRepository;
        this.templateStatusRepository = templateStatusRepository;
        this.recipient = recipient;
        this.textMessageService = textMessageService;
        this.templateMessageService = templateMessageService;
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    @Async
    public List<NotificationStatus> sendText(final Notification notification) {
        return notification.getContacts().parallelStream().map((Recipient to) -> {
            String messageToSend = String.valueOf(this.map(notification, to).get("message"));
            String phoneNumber = this.recipient;
            if (phoneNumber == null) {
                phoneNumber = String.format("+%s%s", to.getPhoneIndex(), to.getPhone());
            }

            final TextMessage textMessage = new TextMessage();
            textMessage.setMessaging_product("whatsapp");
            textMessage.setRecipient_type("individual");
            textMessage.setTo(phoneNumber);
            textMessage.setType("text");
            textMessage.setTo(phoneNumber);
            textMessage.setText(new Text(false, messageToSend));
            WhatsAppResponse response = this.textMessageService.message(textMessage);
            return this.getNotificationStatus(
                    notification,
                    to.getId(),
                    WHATSAPP,
                    response.getMessages().get(0).getId(), //createdMessage.getSid(),
                    "SENT" //createdMessage.getStatus().name()
            );
        }).collect(Collectors.toList());
    }

    @Async
    public List<NotificationStatus> send(final Notification notification) {
        try {
            if (notification.getFrom().isTrial()) {
                return disabledAccountComponents(notification);
            } else {
                return activeAccountComponents(notification);
            }
        } catch (Exception e) {
            log.info("ERREUR LORS DE L'ENVOI d'un message");
            log.error("ERREUR LORS DE L'ENVOI d'un message", e);
        }
        return new ArrayList<>();

    }


    public List<NotificationStatus> activeAccountComponents(final Notification notification) {
        String templateName = "ze_say_hello";
        Template templateInBDD = this.templateRepository.findByName(templateName);
        return notification.getContacts().stream().map((Recipient to) -> {
            final WhatsappTemplate template = new WhatsappTemplate();
            template.setName(templateName);
            template.setLanguage(new Language("fr"));

            final Component component = new Component();
            component.setType("body");
            Map<String, String> params = (Map<String, String>) this.map(notification, to).get("params");
            Map<Integer, String> templateInBDDParams = templateInBDD.getWhatsAppMapping();
            final List<Parameter> parameters = templateInBDDParams.keySet()
                    .parallelStream().map(param -> new Parameter("text", params.get(templateInBDDParams.get(param)), null))
                    .collect(Collectors.toList());

            component.setParameters(parameters);
            template.setComponents(List.of(component));
            final TextMessage textMessage = new TextMessage();
            textMessage.setTemplate(template);
            textMessage.setMessaging_product("whatsapp");
            textMessage.setType("template");
            String phoneNumber = this.recipient;
            if (phoneNumber == null) {
                phoneNumber = String.format("+%s%s", to.getPhoneIndex(), to.getPhone());
            }
            textMessage.setTo(phoneNumber);
            WhatsAppResponse response = this.textMessageService.message(textMessage);
            NotificationStatus notificationStatus = this.getNotificationStatus(
                    notification,
                    to.getId(),
                    WHATSAPP,
                    response.getMessages().get(0).getId(), //createdMessage.getSid(),
                    "QUEUED" //createdMessage.getStatus().name()
            );
            notificationStatus.setProvider("WHATSAPP");
            return notificationStatus;
        }).collect(Collectors.toList());
    }


    public List<NotificationStatus> disabledAccountComponents(final Notification notification) {

        String templateName = "ze_test_template";
        return notification.getContacts().stream().map((Recipient to) -> {
            final Component component = new Component();
            component.setType("body");
            component.setParameters(
                    List.of(
                            new Parameter("text", String.format("%s %s", notification.getFrom().getFirstName(), notification.getFrom().getLastName().toUpperCase()), null)
                    ));

            final WhatsappTemplate template = new WhatsappTemplate();
            template.setName(templateName);
            template.setComponents(List.of(component));
            template.setLanguage(new Language("en"));

            final TextMessage textMessage = new TextMessage();
            textMessage.setTemplate(template);
            textMessage.setMessaging_product("whatsapp");
            textMessage.setType("template");
            String phoneNumber = this.recipient;
            if (phoneNumber == null) {
                phoneNumber = String.format("+%s%s", to.getPhoneIndex(), to.getPhone());
            }
            textMessage.setTo(phoneNumber);

            WhatsAppResponse response = this.textMessageService.message(textMessage);
            return this.getNotificationStatus(
                    notification,
                    to.getId(),
                    WHATSAPP,
                    response.getMessages().get(0).getId(), //createdMessage.getSid(),
                    "SENT" //createdMessage.getStatus().name()
            );
        }).collect(Collectors.toList());
    }


    public WhatsAppResponse createTemplate(Template templateInBDD) {
        List<TemplateComponent> components = templateInBDD.getComponents();
        components.add(
                new TemplateComponent(
                        FOOTER,
                        null,
                        FOOTER_TEXT,
                        null,
                        null
                )
        );
        components = components.stream().peek(templateComponent -> {
            if (templateComponent.getType().equals(BODY)) {
                String text = templateComponent.getText();
                Map<Integer, String> mappings = templateInBDD.getWhatsAppMapping();
                for (Integer key : mappings.keySet()) {
                    text = text.replace(mappings.get(key), "" + key);
                }
                text = text.replaceAll("\\*\\*", "_");
                templateComponent.setText(text);
                TemplateExample templateExample = new TemplateExample();
                templateExample.setBody_text(List.of(mappings.keySet().stream().map(key -> mappings.get(key)).toList()));
                templateComponent.setExample(templateExample);
            }
        }).toList();

        WhatsAppTemplate whatsAppTemplate = new WhatsAppTemplate(
                templateInBDD.getName(),
                null,
                true,
                "fr",
                WHATSAPP,
                UTILITY,
                components
        );
        WhatsAppResponse whatsAppResponse = this.templateMessageService.template(whatsAppTemplate);
        log.info("{}", whatsAppResponse);
        this.templateStatusRepository.save(
                new TemplateStatus(
                        null,
                        templateInBDD.getName(),
                        whatsAppResponse.getId(),
                        templateInBDD.getId(),
                        whatsAppResponse.getStatus(),
                        whatsAppResponse.getCategory(),
                        Instant.now()
                )
        );
        return whatsAppResponse;
    }
    /*
        public void send(final Notification notification) {
            notification.getContacts().parallelStream().forEach((Recipient to) -> {

                final WhatsappTemplate template = new WhatsappTemplate();
                template.setName("hello_world");
                //template.setNamespace("newcourseevent");

                template.setLanguage(new Language("en_US"));

                final TemplateComponent component = new TemplateComponent();
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
                final String eventId = notification.getEventId();
                notificationStatus.setEventId(eventId);
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
            WhatsAppTemplate t = new WhatsAppTemplate("TemplateFromDBName", template, null);
            Writer out = new StringWriter();
            t.process(model, out);
            return out.toString();

        } catch (TemplateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    */

}
