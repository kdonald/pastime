package com.pastime.leagues.season;

import org.codehaus.jackson.annotate.JsonProperty;

import com.pastime.util.Name;

public class PlayerSummary {

    private Integer id;
    
    private Name name;
    
    public PlayerSummary(Integer id, String firstName, String lastName) {
        this.id = id;
        this.name = new Name(firstName, lastName);
    }

    public Integer getId() {
        return id;
    }
    
    public String getName() {
        return name.toString();
    }

    @JsonProperty("first_name")
    public String getFirstName() {
        return name.getFirstName();
    }

    @JsonProperty("last_name")
    public String getLastName() {
        return name.getLastName();
    }
   
}