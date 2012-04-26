package com.pastime.leagues.season;

import com.pastime.util.Name;

public class EmailAddress {
    
    private String value;
    
    private Name name;

    public EmailAddress(String value, Name name) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }            
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name != null ? name.toString() : null;
    }
    
    public String getFirstName() {
        return name != null ? name.getFirstName() : null;       
    }

    public String getLastName() {
        return name != null ? name.getLastName() : null;       
    }
    
}