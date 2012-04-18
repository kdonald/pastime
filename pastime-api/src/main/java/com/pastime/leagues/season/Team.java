package com.pastime.leagues.season;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.leagues.season.AddPlayerForm.EmailAddress;
import com.pastime.util.ErrorReporter;
import com.pastime.util.TeamRoles;

public class Team {

    private final TeamKey key;
    
    private final String name;
    
    private final Roster roster;

    private final TeamMember admin;
    
    private final TeamRepository teamRepository;

    private final URI apiUrl;

    private final URI siteUrl;
    
    public Team(TeamKey key, String name, Roster roster, TeamMember admin, URI apiUrl, URI siteUrl, TeamRepository teamRepository) {
        this.key = key;
        this.name = name;
        this.roster = roster;
        this.admin = admin;
        this.apiUrl = apiUrl;
        this.siteUrl = siteUrl;
        this.teamRepository = teamRepository;        
    }

    public TeamKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
    
    public URI getSiteUrl() {
        return siteUrl;
    }

    public Roster getRoster() {
        return roster;
    }
    
    public URI addPlayer(Integer id) {
        ProposedPlayer player = teamRepository.findProposedPlayer(id);
        return addPlayer(player);
    }
    
    public URI addPlayer(EmailAddress email) {
        ProposedPlayer player = teamRepository.findProposedPlayer(email.getValue());
        if (player != null) {
            return addPlayer(player);
        } else {
            String invite = teamRepository.sendPersonInvite(email, admin, this);
            return UriComponentsBuilder.fromUri(apiUrl).path("/invites/{invite}").buildAndExpand(invite).toUri();           
        }
    }
    
    // internal helpers
    
    private URI addPlayer(ProposedPlayer player) {
        if (teamRepository.alreadyPlaying(player.getId(), key)) {
            throw new AlreadyPlayingException(player.getId(), key);
        }        
        ErrorReporter reporter = new ErrorReporter();
        if (!roster.isAcceptable(player, reporter)) {
            throw new RosterViolationException(key, reporter.getMessage());
        }
        if (admin.sameAs(player)) {
            teamRepository.addTeamMemberRole(key, player.getId(), TeamRoles.PLAYER);
            return UriComponentsBuilder.fromUri(apiUrl).path("/members/{id}").buildAndExpand(player.getId()).toUri();
        } else {
            String invite = teamRepository.sendPlayerInvite(player, admin, this);
            return UriComponentsBuilder.fromUri(apiUrl).path("/invites/{code}").buildAndExpand(invite).toUri();
        }        
    }
    
}