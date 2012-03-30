package com.pastime.leagues;

import org.codehaus.jackson.annotate.JsonProperty;

import com.pastime.util.Name;

public class Player {

    private Integer id;
    
    private Name name;
    
    private Integer number;

    private String nickname;

    private String link;
    
    private String picture;
    
    public Player(Integer id, String firstName, String lastName, Integer number, String nickname, String link, String picture) {
        this.id = id;
        this.name = new Name(firstName, lastName);
        this.number = number;
        this.nickname = nickname;
        this.link = link;
        this.picture = picture;
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
    
    public String getPicture() {
        return picture;
    }
    
}