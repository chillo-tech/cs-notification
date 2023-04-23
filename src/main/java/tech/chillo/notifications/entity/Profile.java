package tech.chillo.notifications.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Profile {
    protected String id;
    protected String civility;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneIndex;
    protected String phone;
}
