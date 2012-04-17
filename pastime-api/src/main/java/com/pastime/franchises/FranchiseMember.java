package com.pastime.franchises;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.players.Player;

public class FranchiseMember extends LinkedResource {

    private final Player player;
    
    private final Integer number;
    
    private final String nickname;

    private final LocalDate joined;
    
    private final Map<String, FranchiseMemberRole> roles = new HashMap<String, FranchiseMemberRole>();
    
    public FranchiseMember(Player player, Integer number, String nickname, LocalDate joined, String slug,
            URI franchiseApi, URI franchiseSite) {
        super(UriComponentsBuilder.fromUri(franchiseApi).path("/members/{id}").buildAndExpand(player.getId()).toUri());
        this.player = player;
        this.number = number;
        this.nickname = nickname;
        this.joined = joined;
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri());
        addLink("site", site(franchiseSite, player.getId(), slug));
    }

    public Player getPlayer() {
        return player;
    }
    
    public Integer getNumber() {
        return number;
    }

    public String getNickname() {
        return nickname;
    }
    
    public LocalDate getJoined() {
        return joined;
    }
    
    public Map<String, FranchiseMemberRole> getRoles() {
        return roles;
    }
    
    public static URI site(URI franchiseUrl, Integer id, String slug) {
        if (slug != null) {
            return UriComponentsBuilder.fromUri(franchiseUrl).path("/" + slug).build().toUri();
        } else {
            return UriComponentsBuilder.fromUri(franchiseUrl).path("/" + id).build().toUri();
        }        
    }
    
}