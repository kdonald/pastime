package com.pastime.leagues.season;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

import com.pastime.util.Name;

public class InvitedPlayer {

    private String invite;

    private Date sent;
    
    private String email;
    
    private String resendLink;
    
    private Name name;

    private String link;
    
    private String picture;

    private Integer playerId;
    
    public InvitedPlayer(String invite, Date sent, String email, String resendLink, Name name, String link, String picture, Integer playerId) {
        this.invite = invite;
        this.sent = sent;
        this.email = email;
        this.resendLink = resendLink;
        this.name = name;
        this.link = link;
        this.picture = picture;
        this.playerId = playerId;
    }

    public String getInvite() {
        return invite;
    }

    public Date getSent() {
        return sent;
    }

    public String getEmail() {
        return email;
    }

    public String getResendLink() {
        return resendLink;
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

    public String getLink() {
        return link;
    }

    public String getPicture() {
        return picture;
    }

    public Integer getPlayerId() {
        return playerId;
    }
    
}