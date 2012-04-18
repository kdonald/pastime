package com.pastime.leagues.season;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.players.Player;
import com.pastime.util.Authorized;
import com.pastime.util.ErrorBody;
import com.pastime.util.Principal;

@Controller
@RequestMapping("/leagues/{league}/seasons/{season}")
public class SeasonController {

    private final TeamRepository teamRepository;
    
    @Inject
    public SeasonController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @RequestMapping(value="/teams", method=RequestMethod.POST)
    @Authorized("teams")
    public ResponseEntity<Object> createTeam(@Valid TeamForm teamForm, Principal principal) {
        URI link = teamRepository.createTeam(teamForm, principal.getPlayerId());        
        return created(link);
    }

    @RequestMapping(value="/teams/{team}/player-search", method=RequestMethod.GET, params="name", produces="application/json")
    @Authorized("teams")
    public @ResponseBody List<Player> playerSearch(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @PathVariable("team") Integer number, 
            @RequestParam("name") String name, Principal principal) {
        return teamRepository.searchPlayers(new TeamKey(league, season, number), name, principal.getPlayerId());
    }
    
    @RequestMapping(value="/teams/{team}/players", method=RequestMethod.POST)
    @Authorized("teams")
    public ResponseEntity<? extends Object> addPlayer(@PathVariable("league") Integer league,
            @PathVariable("season") Integer season, @PathVariable("team") Integer number,
            AddPlayerForm playerForm, Principal principal) {
        Team team;
        try {
            team = teamRepository.getTeamForEditing(new TeamKey(league, season, number), principal.getPlayerId());
        } catch (NoSuchAdminException e) {
            return forbidden("user not an admin for this team");
        }
        URI link;
        if (playerForm.getEmail() != null) {
           link = team.addPlayer(playerForm.getEmailAddress(), principal);
        } else {
           link = team.addPlayer(playerForm.getUserId(), principal);
        }
        return created(link);       
    }

    // internal helpers

    private ResponseEntity<ErrorBody> forbidden(String message) {
        return new ResponseEntity<ErrorBody>(new ErrorBody("user not an admin for this team"), HttpStatus.FORBIDDEN);        
    }
    
    private ResponseEntity<Object> created(URI link) {
        return new ResponseEntity<Object>(headers(link), HttpStatus.CREATED);        
    }
    
    private HttpHeaders headers(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return HttpHeaders.readOnlyHttpHeaders(headers);
    }
    
}