package com.pastime.players;

import org.springframework.util.SlugUtils;

public class TeamPlayer {

    private String url;
    
    private Name name;
    
    private String picture;
    
    private Integer number;
    
    private String nickname;

    public TeamPlayer(String teamUrl, String firstName, String lastName, String picture, Integer number, String nickname) {
        this.url = teamUrl + "/" + SlugUtils.toSlug(nickname);
        this.name = new Name(firstName, lastName);
        this.picture = picture;
        this.number = number;
        this.nickname = nickname;
    }

    public String getUrl() {
        return url;
    }
    
    public String getFullName() {
        return name.toString();
    }
    
    public String getPicture() {
        return picture;
    }

    public Integer getNumber() {
        return number;
    }

    public String getNickname() {
        return nickname;
    }    
    
}
