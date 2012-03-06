package com.pastime.players;

import static org.springframework.jdbc.core.SqlStatements.use;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class TeamsController {

    private JdbcTemplate jdbcTemplate;

    private JavaMailSender mailSender;

    private StringTemplateLoader templateLoader;

    public TeamsController(JdbcTemplate jdbcTemplate, JavaMailSender mailSender, StringTemplateLoader templateLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
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
        SqlRowSet team = findTeam(id);
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
    
    @RequestMapping(value="/teams/{id}/players", method=RequestMethod.POST)
    @Transactional    
    public ResponseEntity<? extends Object> addPlayer(@PathVariable Integer id, final PlayerForm form) {
        if (form.getId() != null && form.getEmail() == null || form.getEmail().length() == 0) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("no player id or email address provided"), HttpStatus.BAD_REQUEST);            
        }
        SqlRowSet team = findTeam(id);
        if (!team.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid team id"), HttpStatus.BAD_REQUEST);                        
        }        
        Player admin = SecurityContext.getCurrentPlayer();
        if (!isAdmin(id, admin.getId())) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("you're not an admin for this team"), HttpStatus.FORBIDDEN);            
        }
        if (form.getEmail() != null) {
            SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, e.email FROM player_emails e INNER JOIN players p ON e.player = p.id WHERE e.email = ?", form.getEmail());
            if (player.last()) {
                sendPlayerInvite(team, player);                 
            } else {
                sendPersonInvite(team, form.getEmail());
            }
        } else {
            SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, e.email FROM players p INNER JOIN player_emails e ON p.id = e.player WHERE p.id = ? AND e.primary_email = true", form.getEmail());
            if (!player.last()) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid player id"), HttpStatus.BAD_REQUEST);                
            }
            sendPlayerInvite(team, player);
        }
        return new ResponseEntity<String>((String) null, HttpStatus.ACCEPTED);
    }

    private SqlRowSet findTeam(Integer id) {
        return jdbcTemplate.queryForRowSet("SELECT t.id, t.name, t.sport, t.logo, u.username FROM teams t LEFT OUTER JOIN usernames u on t.id = u.team WHERE t.id = ?", id);        
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
        List<TeamPlayer> players = jdbcTemplate.query("SELECT p.id, p.first_name, p.last_name, p.picture, t.picture as picture_for_team, t.number, t.nickname, t.captain FROM team_players t INNER JOIN players p on t.player = p.id WHERE team = ? ORDER BY p.last_name", new RowMapper<TeamPlayer>() {
            public TeamPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TeamPlayer(teamUrl, rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), getPicture(rs), rs.getInt("number"), rs.getString("nickname"));
            }
            private String getPicture(ResultSet rs) throws SQLException {
                String picture = rs.getString("picture_for_team");
                if (picture == null) {
                    picture = rs.getString("picture");
                }
                return picture;
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

    private void sendPlayerInvite(final SqlRowSet team, final SqlRowSet player) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(player.getString("email"), player.getString("first_name") + " " + player.getString("last_name")));
               invite.setSubject("Confirm your " + team.getString("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(3, 1);
               model.put("name", player.getString("first_name"));
               model.put("adminUrl", "http://pastime.com/team/2/fish");               
               model.put("admin", "Brian Fisher");
               model.put("sport", team.getString("sport"));               
               model.put("teamUrl", url(team));
               model.put("team", team.getString("name"));               
               model.put("code", "123456");
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
    }

    private void sendPersonInvite(final SqlRowSet team, final String email) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(email));
               invite.setSubject("Confirm your " + team.getString("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(3, 1);
               model.put("name", "Hello");
               model.put("adminUrl", "http://pastime.com/team/2/fish");
               model.put("admin", "Brian Fisher");
               model.put("sport", team.getString("sport"));
               model.put("teamUrl", url(team));
               model.put("team", team.getString("name"));               
               model.put("code", "123456");
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
    }

    // cglib ceremony
    public TeamsController() {}
}
