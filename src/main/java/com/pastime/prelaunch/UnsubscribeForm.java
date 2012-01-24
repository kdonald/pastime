package com.pastime.prelaunch;

import org.hibernate.validator.constraints.Email;

public class UnsubscribeForm {
    
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }
    
}
