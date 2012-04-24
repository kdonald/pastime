package com.pastime.leagues.season;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.util.ErrorReporter;

public class EditableTeam {

    private final TeamKey key;
    
    private final String name;
    
    private final String sport;
    
    private final Roster roster;

    private final TeamMember admin;
    
    private final TeamRepository teamRepository;

    private final URI apiUrl;

    private final URI siteUrl;
    
    public EditableTeam(TeamKey key, String name, String sport, Roster roster, TeamMember admin, URI apiUrl, URI siteUrl, TeamRepository teamRepository) {
        this.key = key;
        this.name = name;
        this.sport = sport;
        this.roster = roster;
        this.admin = admin;
        this.apiUrl = apiUrl;
        this.siteUrl = siteUrl;
        this.teamRepository = teamRepository;        
    }

    public Integer getLeague() {
        return key.getLeague();
    }

    public Integer getSeason() {
        return key.getSeason();
    }
    
    public Integer getNumber() {
        return key.getNumber();
    }
    
    public String getName() {
        return name;
    }

    public String getSport() {
        return sport;
    }

    public URI getApiUrl() {
        return apiUrl;
    }

    public URI getSiteUrl() {
        return siteUrl;
    }

    public Roster getRoster() {
        return roster;
    }
    
    public URI addPlayer(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        ProposedPlayer player = teamRepository.findProposedPlayer(id);
        return addPlayer(player);
    }
    
    public URI addPlayer(EmailAddress email) {
        ProposedPlayer player = teamRepository.findProposedPlayer(email.getValue());
        if (player != null) {
            return addPlayer(player);
        } else {
            return teamRepository.sendPersonInvite(email, admin, this);
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
            teamRepository.addTeamMemberRole(key, player.getId(), TeamMemberRole.PLAYER);
            return UriComponentsBuilder.fromUri(apiUrl).path("/members/{id}").buildAndExpand(player.getId()).toUri();
        } else {
            return teamRepository.sendPlayerInvite(player, admin, this);
        }        
    }
    
}