package tech.chillo.csnotifications.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TemplateParams {
    private String activationLabel;
    private String activationLink;
    private String name;
    private String startDate;
    private String textColor;
}
