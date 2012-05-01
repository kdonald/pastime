package com.pastime.leagues.season;

import java.net.URI;

import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.franchises.Franchise;

public class Team extends LinkedResource {
    
    private final TeamKey key;
    
    private final String name;

    public Team(TeamKey key, String name, String organization, String league, String season, String slug, Integer franchise, URI apiUrl, URI siteUrl) {
        super(api(apiUrl, key));
        this.key = key;
        this.name = name;
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri());
        addLink("players", UriComponentsBuilder.fromUri(getUrl()).path("/members").queryParam("role", TeamMemberRole.PLAYER).build().toUri());
        addLink("invites", UriComponentsBuilder.fromUri(getUrl()).path("/invites").build().toUri());
        addLink("site", site(siteUrl, organization, league, key.getSeason(), season, slug));
        if (franchise != null) {
            addLink("franchise", Franchise.api(apiUrl, franchise));
        }     
        addLink("season", UriComponentsBuilder.fromUri(apiUrl).path("/leagues/{league}/seasons/{season}").buildAndExpand(key.getLeague(), key.getSeason()).toUri());
        addLink("season_site", UriComponentsBuilder.fromUri(siteUrl).path("/{organization}/{league}/{season}").buildAndExpand(organization, league, season).toUri());
        addLink("new_player_search", UriComponentsBuilder.fromUri(getUrl()).path("/new-member-search").queryParam("role", TeamMemberRole.PLAYER).build().toUri());
    }

    public Integer getNumber() {
        return key.getNumber();
    }
    
    public String getName() {
        return name;
    }
    
    public static URI api(URI apiUrl, TeamKey key) {
        return UriComponentsBuilder.fromUri(apiUrl).path("/leagues/{league}/seasons/{season}/teams/{number}").
                buildAndExpand(key.getLeague(), key.getSeason(), key.getNumber()).toUri();        
    }

    public static URI site(URI siteUrl, String organization, String league, Integer seasonNumber, String season, String slug) {
        return UriComponentsBuilder.fromUri(siteUrl).path("/{organization}/{league}/{season}/{team}").
                buildAndExpand(organization, league, seasonPath(seasonNumber, season), slug).toUri();
    }
    
    private static Object seasonPath(Integer seasonNumber, String seasonSlug) {
        return seasonSlug != null ? seasonSlug : seasonNumber;
    }
   
}