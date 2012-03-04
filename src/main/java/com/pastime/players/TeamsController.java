package com.pastime.players;

import static org.springframework.jdbc.core.SqlStatements.use;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class TeamsController {

    private JdbcTemplate jdbcTemplate;

    public TeamsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value="/teams/create", method=RequestMethod.GET)
    public String createTeam(Model model) {
        List<String> sports = jdbcTemplate.queryForList("SELECT name FROM sports order by name", String.class);
        model.addAttribute("sports", sports);
        return "teams/create";
    }

    @RequestMapping(value="/teams", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> createTeam(@Valid TeamForm teamForm) {
        Integer teamId = (Integer) use(jdbcTemplate).insert("INSERT INTO teams (name, sport) VALUES (?, ?)", teamForm.getName(), teamForm.getSport());
        Player player = SecurityContext.getCurrentPlayer();
        jdbcTemplate.update("INSERT INTO team_admins (team, player) VALUES (?, ?)", teamId, player.getId());
        Team team = new Team(teamId);        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(UriComponentsBuilder.fromHttpUrl("http://pastime.com/teams/{id}").buildAndExpand(team.getId()).toUri());
        return new ResponseEntity<Object>(team, headers, HttpStatus.OK);
    }
    
    @RequestMapping(value="/teams/{id}", method=RequestMethod.GET)
    public String team(@PathVariable Integer id, Model model, HttpServletResponse response) throws IOException {
        SqlRowSet team = jdbcTemplate.queryForRowSet("SELECT t.id, t.name, t.sport, t.logo, u.username FROM teams t LEFT OUTER JOIN usernames u on t.id = u.team WHERE t.id = ?", id);
        if (notFound(team, response)) {
            return null;
        }
        model.addAttribute("id", team.getString("id"));
        model.addAttribute("name", team.getString("name"));
        model.addAttribute("sport", team.getString("sport"));
        model.addAttribute("url", url(team));
        model.addAttribute("logo", logo(team));
        addPlayers(id, model);
        addAdmin(id, model);
        return "teams/team";
    }
    
    @RequestMapping(value="/teams/{team}/players", method=RequestMethod.POST)
    @Transactional    
    public ResponseEntity<? extends Object> addPlayer(@PathVariable Integer team, @Valid PlayerForm form) {
        Integer player = jdbcTemplate.queryForInt("SELECT player FROM player_emails WHERE email = ?", form.getEmail());
        jdbcTemplate.update("INSERT INTO team_players (team, player) VALUES (?, ?)", team, player);        
        return new ResponseEntity<String>((String) null, HttpStatus.NO_CONTENT);
    }

    private boolean notFound(SqlRowSet row, HttpServletResponse response) throws IOException {
        if (!row.last()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return true;
        }
        return false;
    }
    
    private String url(SqlRowSet row) {
        String username = row.getString("username");
        if (username != null) {
           return "http://pastime.com/" + username;
        } else {
            return "http://pastime.com/teams/" + row.getInt("id");
        }
    }
    
    private String logo(SqlRowSet row) {
        String logo = row.getString("logo");
        if (logo == null) {
            logo = "http://pastime.com/static/images/default-team-logo.png";
        }
        return logo;
    }

    private void addPlayers(Integer team, Model model) {
        final String teamUrl = (String) model.asMap().get("url");
        List<TeamPlayer> players = jdbcTemplate.query("SELECT p.id, p.first_name, p.last_name, p.picture, t.picture as team_picture, t.number, t.nickname, t.captain FROM team_players t INNER JOIN players p on t.player = p.id WHERE team = ? ORDER BY p.last_name", new RowMapper<TeamPlayer>() {
            public TeamPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
                String picture = rs.getString("team_picture");
                if (picture == null) {
                    picture = rs.getString("picture");
                }
                return new TeamPlayer(teamUrl, rs.getString("first_name"), rs.getString("last_name"), picture, rs.getInt("number"), rs.getString("nickname"));
            }
        }, team);
        model.addAttribute("players?", !players.isEmpty());
        model.addAttribute("players", players);        
    }

    private void addAdmin(Integer team, Model model) {
        Player player = SecurityContext.getCurrentPlayer();
        if (player != null) {
            model.addAttribute("admin", isAdmin(team, player.getId()));
        }        
    }
    private boolean isAdmin(Integer team, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_admins WHERE team = ? and player = ?)", Boolean.class, team, player);
    }
    
    // cglib ceremony
    public TeamsController() {}
}
