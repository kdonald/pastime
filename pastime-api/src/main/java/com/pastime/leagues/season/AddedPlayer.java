package com.pastime.leagues.season;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

import com.pastime.util.Name;

public class AddedPlayer {

    private Integer id;
    
    private Date joinedDate;
    
    private Name name;

    private Integer number;
    
    private String nickname;
    
    private String link;
    
    private String picture;

    public AddedPlayer(Integer id, Date joinedDate, Name name, Integer number, String nickname, String link, String picture) {
        this.id = id;
        this.joinedDate = joinedDate;
        this.name = name;
        this.number = number;
        this.nickname = nickname;
        this.link = link;
        this.picture = picture;
    }

    public Integer getId() {
        return id;
    }
    
    public Date getJoinedDate() {
        return joinedDate;
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