package com.pastime.leagues.season;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.util.ErrorReporter;
import com.pastime.util.Name;

public class EditableTeam {

    private final TeamKey key;
    
    private final String name;
    
    private final String sport;
    
    private final Roster roster;

    private final boolean rosterFrozen;

    private final Integer franchise;
    
    private final URI apiUrl;

    private final URI siteUrl;

    private TeamMember admin;

    private final TeamRepository teamRepository;

    public EditableTeam(TeamKey key, String name, String sport, Roster roster, Boolean rosterFrozen,
            Integer franchise, String organization, String league, Integer seasonNumber, String season, String slug,
            URI apiUrl, URI siteUrl, TeamRepository teamRepository) {
        this.key = key;
        this.name = name;
        this.sport = sport;
        this.roster = roster;
        this.rosterFrozen = rosterFrozen;
        this.franchise = franchise;
        this.apiUrl = Team.api(apiUrl, key);
        this.siteUrl = Team.site(siteUrl, organization, league, seasonNumber, season, slug);
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

    public AddMemberResult addOrInvitePlayer(AddMemberForm form) {
        assertRosterNotFrozen();        
        if (form.getEmail() != null) {
            ProposedPlayer player = teamRepository.findProposedPlayer(form.getEmail());
            if (player != null) {
                if (admin.getId() == player.getId()) {
                    return AddMemberResult.confirmed(addPlayer(player));
                } else {
                    return AddMemberResult.invited(invitePlayer(player));                    
                }
            } else {
                return AddMemberResult.invited(invitePerson(form.createEmailAddress()));
            }            
         } else {
            Integer playerId = form.getId();
            if (playerId == null) {
                playerId = admin.getId();
            }
            if (admin.getId() == playerId) {
                return AddMemberResult.confirmed(addPlayer(playerId));                   
            } else {
                return AddMemberResult.invited(invitePlayer(playerId));                   
            }
         }        
    }

    public URI addPlayer(Integer id) {
        assertRosterNotFrozen(); 
        return addPlayer(teamRepository.findProposedPlayer(id));
    }

    // internal helpers

    private URI addPlayer(ProposedPlayer player) {
        vetPlayer(player);
        FranchiseMember franchise = teamRepository.findFranchiseMember(player.getId(), this.franchise);
        if (!teamRepository.isMember(player.getId(), key)) {
            teamRepository.addMember(player.getId(), key, franchise);
        }
        teamRepository.addTeamMemberRole(key, player.getId(), TeamMemberRole.PLAYER);
        return UriComponentsBuilder.fromUri(apiUrl).path("/members/{id}").queryParam("role", TeamMemberRole.PLAYER).buildAndExpand(player.getId()).toUri();        
    }
    
    private URI invitePlayer(Integer id) {
        return invitePlayer(teamRepository.findProposedPlayer(id));
    }
    
    private URI invitePerson(EmailAddress email) {
        return teamRepository.sendPersonInvite(email, admin, this, TeamMemberRole.PLAYER);        
    }
    
    private void assertRosterNotFrozen() {
        if (rosterFrozen) {
            throw new RosterFrozenException();
        }
    }

    private void vetPlayer(ProposedPlayer player) {
        ErrorReporter reporter = new ErrorReporter();
        if (!roster.isAcceptable(player, reporter)) {
            throw new RosterViolationException(key, reporter.getMessage());
        }        
    }

    private URI invitePlayer(ProposedPlayer player) {
        assertNotAlreadyPlaying(player.getId());
        assertNotAlreadyInvited(player.getId());        
        vetPlayer(player);
        return teamRepository.sendPlayerInvite(player, admin, this);        
    }

    private void assertNotAlreadyPlaying(Integer id) {
        if (teamRepository.alreadyPlaying(id, key)) {
            throw new AlreadyPlayingException(id, key);
        }        
    }

    private void assertNotAlreadyInvited(Integer id) {
        // TODO
    }

    void setAdmin(Integer id, Name name, Integer number, String nickname, String slug) {
        this.admin = new TeamMember(id, TeamMemberRole.ADMIN, name, number, nickname, slug, getApiUrl(), getSiteUrl());
    }
    
}