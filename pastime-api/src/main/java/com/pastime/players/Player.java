package com.pastime.players;

import java.net.URI;

import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.util.Gender;
import com.pastime.util.Name;

public class Player extends LinkedResource {
    
    private final Integer id;

    private final Name name;

    private final Gender gender;
    
    private final LocalDate birthday;
    
    public Player(Integer id, Name name, Gender gender, LocalDate birthday, String username, URI apiUrl, URI siteUrl) {
        super(UriComponentsBuilder.fromUri(apiUrl).path("/players/" + id).build().toUri());
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.birthday = birthday;
        addLink("franchises", UriComponentsBuilder.fromUri(getUrl()).path("/franchises").build().toUri());
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri());
        addLink("site", siteLink(siteUrl, id, username));
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

    public Gender getGender() {
        return gender;
    }
    
    public Integer getAge() {
        return Years.yearsBetween(new LocalDate(), birthday).getYears();
    }
    
    public static URI siteLink(URI siteUrl, Integer id, String username) {
        if (username != null) {
            return UriComponentsBuilder.fromUri(siteUrl).path("/{username}").buildAndExpand(username).toUri();
        } else {
            return UriComponentsBuilder.fromUri(siteUrl).path("/players/{id}").buildAndExpand(id).toUri();            
        }
    }
    
}