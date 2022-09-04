package tech.chillo.csnotifications.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Profile {
    protected String civility;
    protected String firstname;
    protected String lastname;
    protected String email;
    protected String phone;
}
