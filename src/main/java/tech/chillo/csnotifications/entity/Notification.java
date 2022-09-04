package tech.chillo.csnotifications.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "CS_MESSAGE")
public class Notification {
    @Id
    private String id;
    private String subject;
    private String template;
    private Sender from;
    private Set<Recipient> to;
    private Set<Recipient> cc;
    private Set<Recipient> cci;
    private TemplateParams params;
    private Instant creation;

    public Notification() {
        this.cc = new HashSet<>();
        this.cci = new HashSet<>();
    }
}
