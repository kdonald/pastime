package com.pastime.leagues;

import org.codehaus.jackson.annotate.JsonProperty;

import com.pastime.util.Name;

public class PlayerThumbnail {

    private Integer id;
    
    private Name name;
    
    private String link;
    
    private String picture;
    
    public PlayerThumbnail(Integer id, String firstName, String lastName, String link, String picture) {
        this.id = id;
        this.name = new Name(firstName, lastName);
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

    public String getLink() {
        return link;
    }
    
    public String getPicture() {
        return picture;
    }
    
}