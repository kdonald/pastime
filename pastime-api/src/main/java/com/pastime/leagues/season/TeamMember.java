package com.pastime.leagues.season;

import java.net.URI;

import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pastime.util.Name;

public class TeamMember extends LinkedResource {

    private final Integer id;
    
    private final Name name;
    
    private final Integer number;
    
    private final String nickname;
    
    public TeamMember(Integer id, Name name, Integer number, String nickname, String slug, URI teamApi, URI teamSite) {
        super(api(teamApi, id));
        this.id = id;
        this.name = name;
        this.number = number;
        this.nickname = nickname;
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri());
        addLink("site", site(teamSite, id, slug));
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

    // static factory methods
    
    public static URI api(URI teamApi, Integer id) {
        return UriComponentsBuilder.fromUri(teamApi).path("/members/{id}").queryParam("role", TeamMemberRole.PLAYER).buildAndExpand(id).toUri();
    }
    
    public static URI site(URI teamSite, Integer id, String slug) {
        if (slug != null) {
            return UriComponentsBuilder.fromUri(teamSite).path("/{slug}").buildAndExpand(slug).toUri();
        } else {
            return UriComponentsBuilder.fromUri(teamSite).path("/{id}").buildAndExpand(id).toUri();            
        }        
    }

}