package com.pastime.leagues.season;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.players.Player;
import com.pastime.util.Authorized;
import com.pastime.util.Principal;

@Controller
@RequestMapping("/leagues/{league}/seasons/{season}")
public class SeasonController {

    private TeamRepository teamRepository;
    
    @Inject
    public SeasonController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @RequestMapping(value="/teams", method=RequestMethod.POST)
    @Authorized("teams")
    public ResponseEntity<Object> createTeam(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @Valid CreateTeamForm form, Principal principal) {
        URI link = teamRepository.createTeam(new SeasonKey(league, season), form, principal.getPlayerId());        
        return created(link);
    }
    
    @RequestMapping(value="/teams/{team}", method=RequestMethod.GET, produces="application/json")
    public @ResponseBody Team team(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @PathVariable("team") Integer number) {
        return teamRepository.findTeam(new TeamKey(league, season, number));        
    }

    @RequestMapping(value="/teams/{team}/new-member-search", method=RequestMethod.GET, params="name", produces="application/json")
    @Authorized("teams")
    public @ResponseBody List<Player> newMemberSearch(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @PathVariable("team") Integer number, 
            @RequestParam("name") String name, @RequestParam(value="role", defaultValue="Player") String role, Principal principal) {
        return teamRepository.searchPlayers(new TeamKey(league, season, number), name, principal.getPlayerId());
    }
    
    @RequestMapping(value="/teams/{team}/members", method=RequestMethod.POST)
    @Authorized("teams")
    @Transactional
    public ResponseEntity<? extends Object> addMember(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @PathVariable("team") Integer number,
            AddMemberForm playerForm, Principal principal) {
        EditableTeam team = teamRepository.getTeamForEditing(new TeamKey(league, season, number), principal.getPlayerId());
        URI link = addMember(playerForm, team, principal);
        return created(link);       
    }
    
    @RequestMapping(value="/teams/{team}/members/{id}", method=RequestMethod.GET, produces="application/json")
    public @ResponseBody TeamMember member(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @PathVariable("team") Integer team, @PathVariable("id") Integer id) {
        return teamRepository.findTeamMember(new TeamKey(league, season, team), id);
    }

    // internal helpers

    private URI addMember(AddMemberForm playerForm, EditableTeam team, Principal principal) {
        if (playerForm.getEmail() != null) {
           return team.addPlayer(playerForm.createEmailAddress());
        } else {
           Integer id = playerForm.getId();
           if (id == null) {
               id = principal.getPlayerId();
           }
           return team.addPlayer(id);
        }        
    }
    
    private ResponseEntity<Object> created(URI link) {
        return new ResponseEntity<Object>(headers(link), HttpStatus.CREATED);        
    }
    
    private HttpHeaders headers(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return HttpHeaders.readOnlyHttpHeaders(headers);
    }
    
    // cglib ceremony 
    public SeasonController() {}
}