package com.pastime.leagues.season;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.util.Name;

public class TeamMember {

    private final Integer id;
    
    private final Name name;
    
    private final Integer number;
    
    private final String nickname;
    
    private final URI siteUrl;

    public TeamMember(Integer id, Name name, Integer number, String nickname, String slug, URI teamSite) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.nickname = nickname;
        if (slug != null) {
            this.siteUrl = UriComponentsBuilder.fromUri(teamSite).path("/{slug}").buildAndExpand(slug).toUri();
        } else {
            this.siteUrl = UriComponentsBuilder.fromUri(teamSite).path("/{id}").buildAndExpand(id).toUri();            
        }
    }

    public Integer getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Integer getNumber() {
        return number;
    }

    public String getNickname() {
        return nickname;
    }

    public URI getSiteUrl() {
        return siteUrl;
    }

    public boolean sameAs(ProposedPlayer player) {
        return id.equals(player.getId());
    }

}