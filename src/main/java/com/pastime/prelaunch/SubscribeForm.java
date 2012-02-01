package com.pastime.prelaunch;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.StringUtils;

public class SubscribeForm {
    
    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    @Email
    private String email;

    private String r;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = StringUtils.capitalize(firstName.trim());
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = StringUtils.capitalize(lastName.trim());
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r.trim().toLowerCase();
    }    

}