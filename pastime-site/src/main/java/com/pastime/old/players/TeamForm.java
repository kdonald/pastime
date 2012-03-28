package com.pastime.old.players;

import org.hibernate.validator.constraints.NotEmpty;

public class TeamForm {
    
    @NotEmpty
    private String name;
    
    @NotEmpty
    private String sport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport.trim();
    }
   
}
