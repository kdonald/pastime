package com.pastime.leagues.season;

import com.pastime.util.Name;

public class EmailAddress {
    
    private String value;
    
    private Name displayName;

    public EmailAddress(String value, Name displayName) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }            
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public Name getDisplayName() {
        return displayName;
    }
    
}