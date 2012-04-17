package com.pastime.franchises;

import java.net.URI;

import org.joda.time.LocalDate;
import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

public class Franchise extends LinkedResource {
    
    private final Integer id;
    
    private final String name;

    private final String sport;
    
    private final LocalDate founded;

    private final LocalDate joined;
    
    private final String username;
    
    public Franchise(Integer id, String name, String sport, LocalDate founded, LocalDate joined, 
            String username, URI apiUrl, URI siteUrl) {
        super(api(apiUrl, id));
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.founded = founded;
        this.joined = joined;
        this.username = username;
        addLink("founder", UriComponentsBuilder.fromUri(getUrl()).path("/founder").build().toUri());
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri()); 
        addLink("members", UriComponentsBuilder.fromUri(getUrl()).path("/members").build().toUri());
        addLink("site", site(siteUrl, id, username));
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public String getSport() {
        return sport;
    }

    public LocalDate getFounded() {
        return founded;
    }

    public LocalDate getJoined() {
        return joined;
    }
    
    public String getUsername() {
        return username;
    }

    // internal helpers
    
    public static URI site(URI siteUrl, Integer id, String username) {
        if (username != null) {
            return UriComponentsBuilder.fromUri(siteUrl).path("/{username}").buildAndExpand(username).toUri();
        } else {
            return UriComponentsBuilder.fromUri(siteUrl).path("/franchises/{id}").buildAndExpand(id).toUri();
        }        
    }

    public static URI api(URI apiUrl, Integer id) {
        return UriComponentsBuilder.fromUri(apiUrl).path("/franchises/{id}").buildAndExpand(id).toUri();
    }
    
}