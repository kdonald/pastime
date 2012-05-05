package com.pastime.leagues.season;

import java.net.URI;

import org.joda.time.DateTime;
import org.springframework.util.LinkedResource;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.players.Player;
import com.pastime.util.Name;

public class TeamMemberInvite extends LinkedResource {

    private final String code;
    
    private final TeamMemberRole role;
    
    private final Name name;
    
    private final DateTime sent;

    public TeamMemberInvite(String code, Name name, TeamMemberRole role, DateTime sent, Integer id, String username,
            URI teamUrl, URI siteUrl) {
        super(apiUrl(teamUrl, code));
        this.code = code;
        this.role = role;
        this.name = name;
        this.sent = sent;
        addLink("picture", UriComponentsBuilder.fromUri(getUrl()).path("/picture").build().toUri());
        addLink("accept", UriComponentsBuilder.fromUri(getUrl()).queryParam("a", InviteAnswer.ACCEPT).build().toUri());
        addLink("decline", UriComponentsBuilder.fromUri(getUrl()).queryParam("a", InviteAnswer.DECLINE).build().toUri());
        if (id != null) {
            addLink("site", Player.site(siteUrl, id, username));
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name != null ? name.toString() : null;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public DateTime getSent() {
        return sent;
    }

    public static URI apiUrl(URI teamUrl, String code) {
        return UriComponentsBuilder.fromUri(teamUrl).path("/invites/{code}").buildAndExpand(code).toUri();
    }
        
}
