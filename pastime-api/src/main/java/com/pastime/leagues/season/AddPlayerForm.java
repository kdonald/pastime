package com.pastime.leagues.season;

import com.pastime.util.Name;


public class AddPlayerForm {
    
    private Integer userId;
    
    private String email;

    private String name;
    
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

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

    public EmailAddress getEmailAddress() {
        return new EmailAddress(email, Name.valueOf(name));
    }
    
    public static class EmailAddress {
        
        private String value;
        
        private Name displayName;

        public EmailAddress(String value, Name displayName) {
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
}
