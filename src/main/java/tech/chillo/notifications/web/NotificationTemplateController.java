package tech.chillo.notifications.web;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.chillo.notifications.entity.NotificationTemplate;
import tech.chillo.notifications.enums.Application;
import tech.chillo.notifications.service.NotificationTemplateService;

import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@RestController
@RequestMapping(path = "v1/template", produces = APPLICATION_JSON_VALUE)
public class NotificationTemplateController {

    private NotificationTemplateService notificationTemplateService;

    @PostMapping
    public void create(@RequestParam("application") Application application,
                       @RequestBody Set<NotificationTemplate> templates) {
        this.notificationTemplateService.create(application, templates);
    }

    @PutMapping(path = "{id}")
    public void update(@RequestParam("application") String application,
                       @RequestBody NotificationTemplate templateRequest) {
    }


}
