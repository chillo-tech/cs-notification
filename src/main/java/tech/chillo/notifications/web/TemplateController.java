package tech.chillo.notifications.web;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tech.chillo.notifications.entity.template.Template;
import tech.chillo.notifications.service.template.TemplateService;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@RestController
@RequestMapping(path = "v1/template", produces = APPLICATION_JSON_VALUE)
public class TemplateController {
    private TemplateService templateService;

    @ResponseStatus(NO_CONTENT)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void create(@RequestBody final Template template) {

        this.templateService.create(template);
    }

}
