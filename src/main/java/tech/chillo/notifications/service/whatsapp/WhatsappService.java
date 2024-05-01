package tech.chillo.notifications.service.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.entity.NotificationTemplate;
import tech.chillo.notifications.entity.Recipient;
import tech.chillo.notifications.entity.Sender;
import tech.chillo.notifications.entity.TemplateStatus;
import tech.chillo.notifications.entity.template.Template;
import tech.chillo.notifications.entity.template.TemplateComponent;
import tech.chillo.notifications.entity.template.TemplateExample;
import tech.chillo.notifications.entity.template.WhatsAppTemplate;
import tech.chillo.notifications.enums.Application;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.repository.NotificationTemplateRepository;
import tech.chillo.notifications.repository.TemplateRepository;
import tech.chillo.notifications.repository.TemplateStatusRepository;
import tech.chillo.notifications.service.NotificationMapper;
import tech.chillo.notifications.service.mail.WhatsAppMessageService;
import tech.chillo.notifications.service.whatsapp.dto.Component;
import tech.chillo.notifications.service.whatsapp.dto.Image;
import tech.chillo.notifications.service.whatsapp.dto.Language;
import tech.chillo.notifications.service.whatsapp.dto.Parameter;
import tech.chillo.notifications.service.whatsapp.dto.Text;
import tech.chillo.notifications.service.whatsapp.dto.TextMessage;
import tech.chillo.notifications.service.whatsapp.dto.WhatsAppResponse;
import tech.chillo.notifications.service.whatsapp.dto.WhatsappTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.chillo.notifications.data.ApplicationData.CIVILITY_MAPPING;
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
    private final WhatsAppMessageService whatsAppMessageService;

    public WhatsappService(
            final TemplateRepository templateRepository, @Value("${application.recipient.sms:#{null}}") final String recipient,
            final TemplateStatusRepository templateStatusRepository,
            final TextMessageService textMessageService,
            final TemplateMessageService templateMessageService,
            final NotificationTemplateRepository notificationTemplateRepository,
            final WhatsAppMessageService whatsAppMessageService) {
        super(notificationTemplateRepository);
        this.templateRepository = templateRepository;
        this.templateStatusRepository = templateStatusRepository;
        this.recipient = recipient;
        this.textMessageService = textMessageService;
        this.templateMessageService = templateMessageService;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.whatsAppMessageService = whatsAppMessageService;
    }

    @Async
    public List<NotificationStatus> sendText(final Notification notification) {
        return notification.getContacts().parallelStream().map((final Recipient to) -> {
            final String messageToSend = String.valueOf(this.map(notification, to, WHATSAPP).get("message"));
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
            final WhatsAppResponse response = this.textMessageService.message(textMessage);
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
                return this.disabledAccountComponents(notification);
            } else {
                return this.activeAccountComponents(notification);
            }
        } catch (final Exception e) {
            log.error("ERREUR LORS DE L'ENVOI d'un message", e);
            e.printStackTrace();
        }
        return new ArrayList<>();

    }


    public List<NotificationStatus> activeAccountComponents(final Notification notification) {
        final String templateName = "ze_say_hello";
        final Template templateInBDD = this.templateRepository.findByName(templateName);
        return notification.getContacts().stream().map((final Recipient to) -> {

            final Component component = new Component();
            component.setType("body");
            final Map<String, String> params = (Map<String, String>) this.map(notification, to, WHATSAPP).get("params");
            final Map<Integer, String> templateInBDDParams = templateInBDD.getWhatsAppMapping();
            final List<Parameter> parameters = templateInBDDParams.keySet()
                    .stream().map(param -> new Parameter("text", params.get(templateInBDDParams.get(param)), null))
                    .collect(Collectors.toList());
            component.setParameters(parameters);

            final WhatsappTemplate template = new WhatsappTemplate();
            template.setName(templateName);
            template.setLanguage(new Language("fr"));
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
            final WhatsAppResponse response = this.textMessageService.message(textMessage);
            final NotificationStatus notificationStatus = this.getNotificationStatus(
                    notification,
                    to.getId(),
                    WHATSAPP,
                    response.getMessages().get(0).getId(), //createdMessage.getSid(),
                    "SENT" //createdMessage.getStatus().name()
            );
            notificationStatus.setProvider("WHATSAPP");
            return notificationStatus;
        }).collect(Collectors.toList());
    }


    public List<NotificationStatus> disabledAccountComponents(final Notification notification) {

        final String templateName = "ze_test_template";
        return notification.getContacts().stream().map((final Recipient to) -> {
            final Component component = new Component();
            component.setType("body");
            final List<Parameter> parameters = List.of(
                    new Parameter("text", String.format("%s %s", notification.getFrom().getFirstName(), notification.getFrom().getLastName().toUpperCase()), null)
            );
            component.setParameters(parameters);

            final WhatsappTemplate template = new WhatsappTemplate();
            template.setName(templateName);
            template.setLanguage(new Language("en"));
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

            final WhatsAppResponse response = this.textMessageService.message(textMessage);
            final NotificationStatus notificationStatus = this.getNotificationStatus(
                    notification,
                    to.getId(),
                    WHATSAPP,
                    response.getMessages().get(0).getId(), //createdMessage.getSid(),
                    "SENT" //createdMessage.getStatus().name()
            );

            notificationStatus.setProvider("WHATSAPP");
            return notificationStatus;
        }).collect(Collectors.toList());
    }


    public WhatsAppResponse createTemplate(final Template templateInBDD) {
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
                final Map<Integer, String> mappings = templateInBDD.getWhatsAppMapping();
                for (final Integer key : mappings.keySet()) {
                    text = text.replace(mappings.get(key), "" + key);
                }
                text = text.replaceAll("\\*\\*", "_");
                templateComponent.setText(text);
                final TemplateExample templateExample = new TemplateExample();
                templateExample.setBody_text(List.of(mappings.keySet().stream().map(key -> mappings.get(key)).toList()));
                templateComponent.setExample(templateExample);
            }
        }).toList();

        final WhatsAppTemplate whatsAppTemplate = new WhatsAppTemplate(
                templateInBDD.getName(),
                null,
                true,
                "fr",
                WHATSAPP,
                UTILITY,
                components
        );
        final WhatsAppResponse whatsAppResponse = this.templateMessageService.template(whatsAppTemplate);
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

    public List<NotificationStatus> sendFromParams(final Map<String, Object> notificationParams, final NotificationType notificationType) {
        final Map<String, Object> invitation = (Map<String, Object>) notificationParams.get("invitation");
        final Map<String, Object> template = (Map<String, Object>) invitation.get("template");
        final String whatsappTemplateName = (String) notificationParams.get("whatsappTemplateName");
        final String notificationTemplate = (String) notificationParams.get("notificationTemplate");
        final String eventName = (String) notificationParams.get("eventName");
        final String application = (String) notificationParams.get("application");

        final Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(notificationParams.get("guest"));
        final Recipient to = gson.fromJson(jsonElement, Recipient.class);
        jsonElement = gson.toJsonTree(notificationParams.get("author"));
        final Sender sender = gson.fromJson(jsonElement, Sender.class);

        final Map<String, String> body = Map.of(
                "body", String.format("%s", template.get("text"))
        );
        final NotificationTemplate templateFromDatabase = this.notificationTemplateRepository
                .findByApplicationAndName(
                        application,
                        notificationTemplate
                )
                .orElseThrow(() -> new IllegalArgumentException(String.format("Aucun template %s n'existe pour %s", Application.valueOf(application), notificationTemplate)));
        final List<Object> schedules = (List<Object>) template.get("schedules");

        final ObjectMapper oMapper = new ObjectMapper();
        final List<String> mappedSchedules = schedules.stream().map(schedule -> {
            final Map<String, Object> map = oMapper.convertValue(schedule, Map.class);
            final Object dateKeys = map.get("date");
            final Map<String, String> dateKeysMapped = oMapper.convertValue(dateKeys, Map.class);

            final String seconds = String.format("%s", dateKeysMapped.get("seconds"));
            final long millis = Long.valueOf(seconds) * 1000;
            final Date date = new Date(millis);
            final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.FRENCH);
            final String formattedDate = sdf.format(date);
            System.out.println(formattedDate);

            return formattedDate; //String.format("%s", dateKeysMapped.get("seconds"));
        }).collect(Collectors.toList());

        final String firstName = to.getFirstName();
        final String formattedFirstName = firstName.isEmpty() ? " " : firstName;
        final String finalTemplate = this.processTemplate(
                Map.of(
                        "title", List.of(template.get("title")),
                        "text", List.of(template.get("text")),
                        "address", List.of(template.get("address")),
                        "guest", List.of(String.format(
                                "%s %s%s %s",
                                CIVILITY_MAPPING.get(to.getCivility()),

                                String.valueOf(formattedFirstName.charAt(0)).toUpperCase(),
                                formattedFirstName.substring(1).toLowerCase(),

                                String.valueOf(to.getLastName().isEmpty() ? "" : to.getLastName()).toUpperCase()
                        )),
                        "schedules", List.of(mappedSchedules),
                        "image", List.of(notificationParams.get("image"))
                ),
                templateFromDatabase.getContent(),
                WHATSAPP
        );

        log.info(" template nem {}", finalTemplate);

        final Component headerCompoenent = new Component();
        headerCompoenent.setType("header");
        final List<Parameter> headerComponentParameters = List.of(
                new Parameter(
                        "image",
                        null,
                        Image.builder().link(String.valueOf(notificationParams.get("image"))).build()
                ));
        headerCompoenent.setParameters(headerComponentParameters);

        final Component bodyComponent = new Component();
        bodyComponent.setType("body");
        final List<Parameter> bodyComponentParameters = List.of(
                new Parameter(
                        "text",
                        String.format(
                                "%s %s%s %s",
                                sender.getCivility() == null ? "" : CIVILITY_MAPPING.get(to.getCivility()),
                                String.valueOf(formattedFirstName.charAt(0)).toUpperCase(),
                                formattedFirstName.substring(1).toLowerCase(),

                                String.valueOf(to.getLastName().isEmpty() ? "" : to.getLastName()).toUpperCase()
                        ).trim(),
                        null
                ),
                new Parameter(
                        "text",
                        eventName,
                        null
                ),
                new Parameter(
                        "text",
                        String.format(
                                "%s",
                                String.join(" | ", mappedSchedules)
                        ),
                        null
                ),
                new Parameter(
                        "text",
                        template.get("address"),
                        null
                ),
                new Parameter(
                        "text",
                        String.format(
                                "%s%s %s",
                                String.valueOf(sender.getFirstName().charAt(0)).toUpperCase(),
                                sender.getFirstName().substring(1).toLowerCase(),
                                sender.getLastName().toUpperCase()
                        ).trim(),
                        null
                )
        );
        bodyComponent.setParameters(bodyComponentParameters);


        final WhatsappTemplate whatsappTemplate = new WhatsappTemplate();
        whatsappTemplate.setName(whatsappTemplateName);
        whatsappTemplate.setLanguage(new Language("fr"));
        whatsappTemplate.setComponents(List.of(headerCompoenent, bodyComponent));

        final TextMessage textMessage = new TextMessage();
        textMessage.setMessaging_product("whatsapp");
        textMessage.setRecipient_type("individual");
        String phoneNumber = this.recipient;
        if (phoneNumber == null) {
            phoneNumber = String.format("+%s%s", to.getPhoneIndex(), to.getPhone());
        }
        textMessage.setTo(phoneNumber);
        textMessage.setType("template");
        textMessage.setTemplate(whatsappTemplate);

        final WhatsAppResponse whatsAppResponse = this.textMessageService.message(textMessage);

        final NotificationStatus notificationStatus = new NotificationStatus();
        notificationStatus.setEventId(String.format("%s", template.get("eventId")));
        notificationStatus.setUserId(to.getId());
        notificationStatus.setChannel(notificationType);
        notificationStatus.setProviderNotificationId(whatsAppResponse.getId());
        notificationStatus.setStatus(whatsAppResponse.getStatus());
        notificationStatus.setCreation(Instant.now());
        return List.of(notificationStatus);
    }


    private BufferedImage createImageFromBytes(final String image) {
        try {
            final byte[] bytes = Base64.getDecoder().decode(image);
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void convertHtmlToImage(final String qr, final String htmlSource) {
        final BufferedImage bufferedImage = new BufferedImage(20000, 2500, BufferedImage.TYPE_INT_RGB);

        final Graphics2D g2d = bufferedImage.createGraphics();
        final Font font = new Font("Ticketing", Font.PLAIN, 120);

        g2d.setFont(font);
        g2d.setColor(Color.DARK_GRAY);
        final BufferedImage qrCode = this.createImageFromBytes(qr);
        g2d.drawImage(qrCode, 0, 0, null);

        g2d.drawString("lkhpfe", 1550, 400);
        g2d.drawString("pk^^zjf^^j^", 1550, 800);
        g2d.drawString("jfpzhjpzozj", 1550, 1100);
        g2d.drawString("ko^^jz^^zj", 1550, 1400);
        g2d.dispose();
        try {
            ImageIO.write(bufferedImage, "png", new File("/Users/chillo/projets/zeeven/data/tickets/test.png"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected String processTemplate(final String application, final String template, final Map<String, List<Object>> params) {
        final String messageToSend;
        final NotificationTemplate notificationTemplate = this.notificationTemplateRepository
                .findByApplicationAndName(application, template)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Aucun template %s n'existe pour %s", template, application)));
        messageToSend = this.processTemplate(params, notificationTemplate.getContent(), WHATSAPP);
        return messageToSend;
    }
}
