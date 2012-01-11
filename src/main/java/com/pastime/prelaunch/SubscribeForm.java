package com.pastime.prelaunch;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class SubscribeForm {
    
    @NotEmpty
    private String name;

    @NotEmpty
    @Email
    private String email;

    private String ref;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRef() {
        return ref;
    }
    
    public void setRef(String ref) {
        this.ref = ref;
    }
    
    public String toString() {
        return "[SubscribeForm name='" + name + "', email='" + email + "', ref='" + ref + "']";
    }

}
