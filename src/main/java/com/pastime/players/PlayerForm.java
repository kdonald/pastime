package com.pastime.players;

import org.hibernate.validator.constraints.NotEmpty;

public class PlayerForm {

    @NotEmpty
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim().toLowerCase();
    }
        
}
