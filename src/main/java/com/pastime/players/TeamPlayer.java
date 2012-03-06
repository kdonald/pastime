package com.pastime.players;


public class TeamPlayer {

    private String url;
    
    private Name name;
    
    private String picture;
    
    private Integer number;
    
    private String nickname;

    public TeamPlayer(String teamUrl, Integer id, String firstName, String lastName, String picture, Integer number, String nickname) {
        this.url = playerUrl(teamUrl, id, nickname);
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

    private String playerUrl(String teamUrl, Integer id, String nickname) {
        if (nickname != null) {
            return teamUrl + "/" + nickname;
        } else {
            return teamUrl + "/" + id;
        }
    }

}
