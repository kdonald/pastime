package com.pastime.leagues.season;

import com.pastime.util.Name;

public class AddMemberForm {
    
    private Integer id;
    
    private String email;

    private String name;
    
    private TeamMemberRole role;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public void setRole(TeamMemberRole role) {
        this.role = role;
    }

    // factory methods
    
    public EmailAddress createEmailAddress() {
        return new EmailAddress(email, Name.valueOf(name));
    }

}
