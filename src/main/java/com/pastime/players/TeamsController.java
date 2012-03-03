package com.pastime.players;

import static org.springframework.jdbc.core.SqlStatements.use;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class TeamsController {

    private JdbcTemplate jdbcTemplate;

    public TeamsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value="/teams", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> createTeam(@Valid TeamForm teamForm) {
        Integer teamId = (Integer) use(jdbcTemplate).insert("INSERT INTO teams (name, sport) VALUES (?, ?)", teamForm.getName(), teamForm.getSport());
        Player player = SecurityContext.getCurrentPlayer();
        jdbcTemplate.update("INSERT INTO team_admins (team, player) VALUES (?, ?)", teamId, player.getId());
        Team team = new Team(teamId);        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.fromHttpUrl("http://pastimebrevard.com/teams/{id}").buildAndExpand(team.getId()).toUri());
        return new ResponseEntity<Object>(team, headers, HttpStatus.OK);
    }
    
    // cglib ceremony
    public TeamsController() {}
}
