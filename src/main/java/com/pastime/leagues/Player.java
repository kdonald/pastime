package com.pastime.leagues;

import org.codehaus.jackson.annotate.JsonProperty;

import com.pastime.players.Name;

public class Player {

    private Integer id;
    
    private String link;
    
    private Name name;
    
    private Integer number;

    private String nickname;
    
    public Player(Integer id, String firstName, String lastName, Integer number, String nickname, String link) {
        this.id = id;
        this.name = new Name(firstName, lastName);
        this.number = number;
        this.nickname = nickname;
        this.link = link;
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

    public Integer getNumber() {
        return number;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getLink() {
        return link;
    }
    
}