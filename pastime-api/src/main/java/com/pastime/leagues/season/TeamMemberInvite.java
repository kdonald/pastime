package com.pastime.leagues.season;

import java.net.URI;

import org.joda.time.DateTime;
import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.players.Player;
import com.pastime.util.Name;

public class TeamMemberInvite extends LinkedResource {

    private final String code;
    
    private final String email;
    
    private final TeamMemberRole role;
    
    private final Name name;
    
    private final DateTime sent;

    public TeamMemberInvite(String code, String email, TeamMemberRole role, Name name, DateTime sent, Integer id, String username,
            URI teamUrl, URI siteUrl) {
        super(apiUrl(teamUrl, code));
        this.code = code;
        this.role = role;
        this.email = email;
        this.name = name;
        this.sent = sent;
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri());
        if (id != null) {
            addLink("site", Player.site(siteUrl, id, username));
        }
    }

    public String getCode() {
        return code;
    }

    public String getEmail() {
        return email;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public String getName() {
        return name != null ? name.toString() : null;
    }
    
    public DateTime getSent() {
        return sent;
    }

    public static URI apiUrl(URI teamUrl, String code) {
        return UriComponentsBuilder.fromUri(teamUrl).path("/invites/{code}").buildAndExpand(code).toUri();
    }
        
}
