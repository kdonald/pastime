package com.pastime.players;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.EmailUtils;

public class SigninForm {

    @NotEmpty
    private String name;

    @NotEmpty
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isEmail() {
        return EmailUtils.isEmail(name);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}