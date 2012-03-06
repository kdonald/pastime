package com.pastime.players;

import static org.springframework.jdbc.core.SqlStatements.use;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
import org.springframework.security.crypto.keygen.InsecureRandomStringGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class TeamsController {

    private JdbcTemplate jdbcTemplate;

    private JavaMailSender mailSender;

    private StringTemplateLoader templateLoader;

    private InsecureRandomStringGenerator inviteGenerator = new InsecureRandomStringGenerator(6);
    
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
        if (!SecurityContext.playerSignedIn()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }        
        Integer teamId = (Integer) use(jdbcTemplate).insert("INSERT INTO teams (name, sport) VALUES (?, ?)", teamForm.getName(), teamForm.getSport());
        Player player = SecurityContext.getCurrentPlayer();
        jdbcTemplate.update("INSERT INTO team_members (team, player) VALUES (?, ?)", teamId, player.getId());
        jdbcTemplate.update("INSERT INTO team_member_roles (team, player, role) VALUES (?, ?, ?)", teamId, player.getId(), TeamRoles.ADMIN);
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
    
    @RequestMapping(value="/teams/{id}/players", method=RequestMethod.POST, produces="application/json")
    @Transactional    
    public ResponseEntity<? extends Object> addPlayer(@PathVariable Integer id, final PlayerForm form) {
        if (!SecurityContext.playerSignedIn()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }
        if (form.getId() != null && form.getEmail() == null || form.getEmail().length() == 0) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("no player id or email address provided"), HttpStatus.BAD_REQUEST);            
        }
        SqlRowSet team = findTeam(id);
        if (!team.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid team id"), HttpStatus.BAD_REQUEST);                        
        }        
        SqlRowSet admin = jdbcTemplate.queryForRowSet("select p.id, p.first_name, p.last_name, t.nickname, t.username FROM team_member_roles r " + 
                "INNER JOIN team_members t ON r.team = t.team AND r.player = t.player INNER JOIN players p ON t.player = p.id WHERE r.team = ? and r.player = ? AND r.role = 'Admin'",
                team.getInt("id"), SecurityContext.getCurrentPlayer().getId());
        if (!admin.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("you're not an admin for this team"), HttpStatus.FORBIDDEN);            
        }        
        if (form.getEmail() != null) {
            if (inviteAlreadySent(team.getInt("id"), form.getEmail())) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("player invite already sent: POST to " + url(team) + "/invites/{code} if you wish to resend"), HttpStatus.CONFLICT);                    
            }            
            PlayerInvite invite = sendInvite(team, admin, form.getEmail());
            return new ResponseEntity<PlayerInvite>(invite, HttpStatus.CREATED);            
        } else {
            SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, e.email FROM players p INNER JOIN player_emails e ON p.id = e.player WHERE p.id = ? AND e.primary_email = true", form.getEmail());
            if (!player.last()) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid player id"), HttpStatus.BAD_REQUEST);                
            }
            PlayerInvite invite = sendPlayerInvite(team, admin, player);
            return new ResponseEntity<PlayerInvite>(invite, HttpStatus.CREATED);            
        }
    }
    
    @RequestMapping(value="/teams/{teamId}/invites/{code}", method=RequestMethod.GET)
    @Transactional
    public String answerInvite(@PathVariable Integer teamId, @PathVariable String code, @RequestParam String a, Model model, HttpServletResponse response) throws IOException {
        SqlRowSet team = findTeam(teamId);
        if (!team.last()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        SqlRowSet invite = jdbcTemplate.queryForRowSet("SELECT email, role, accepted, player FROM team_member_invites WHERE team = ? AND code = ?", teamId, code);
        if (!invite.last()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Boolean accepted = invite.getBoolean("accepted");
        if (!invite.wasNull()) {
            model.addAttribute("answer", accepted);
            return "teams/invite-already-answered";
        }
        a = a.toLowerCase();
        if (a.equals("accept")) {
            Date answered = new Date();
            jdbcTemplate.update("UPDATE team_member_invites set accepted = true, answered_on = ? where team = ? and code = ?", answered, teamId, code);
            String role = invite.getString("role");
            if (role.equals(TeamRoles.PLAYER)) {
                Integer playerId = invite.getInt("player");
                if (playerId == null) {
                    Player currentPlayer = SecurityContext.getCurrentPlayer();
                    if (currentPlayer == null) {
                        // nobody is signed-in and the invite wasn't for an existing user
                        return "signup";
                    }
                    playerId = currentPlayer.getId();
                    if (!emailOnFile(invite.getString("email"), playerId)) {
                      // somebody else is signed-in
                      return "signin";
                    }
                }
                if (!teamMember(teamId, playerId)) {
                    // the player isn't already a team member, make him or her one
                    jdbcTemplate.update("INSERT INTO team_member (team, player) VALUES (?, ?)", teamId, playerId);                    
                }
                jdbcTemplate.update("INSERT INTO team_member_roles (team, player, role, player_status) VALUES (?, ?, 'Player', 'a')", teamId, playerId);
                return "redirect:/teams/" + teamId + "/" + playerId + "/?source=invite_accepted";
            } else {
                throw new UnsupportedOperationException("Only player invites are supported at the moment");
            }
        } else if (a.equals("decline")) {
            model.addAttribute("team", team.getString("name"));
            return "teams/player-invite-decline";
        } else {
            throw new IllegalArgumentException("Not a valid answer");            
        }
    }

    @RequestMapping(value="/teams/{teamId}/invites/{code}", method={ RequestMethod.POST, RequestMethod.DELETE })
    @Transactional
    public ResponseEntity<? extends Object> declineInvite(@PathVariable Integer teamId, @PathVariable String code) {
        SqlRowSet invite = jdbcTemplate.queryForRowSet("SELECT accepted FROM team_member_invites WHERE team = ? AND code = ?", teamId, code);
        if (!invite.last()) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
        invite.getBoolean("accepted");
        if (!invite.wasNull()) {
            return new ResponseEntity<String>("Invite already answered", HttpStatus.CONFLICT);
        }        
        jdbcTemplate.update("UPDATE team_member_invites set accepted = false, answered_on = ? where team = ? and code = ?", new Date(), teamId, code);        
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
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
        String sql = "SELECT p.id, p.first_name, p.last_name, p.picture, t.picture as picture_for_team, t.number, t.nickname, t.username, r.player_status, r.player_captain, r.player_captain_of FROM team_members t " + 
                "INNER JOIN team_member_roles r ON t.team = ? AND t.player = r.player INNER JOIN players p on t.player = p.id WHERE r.team = ? AND r.role = 'Player' ORDER BY p.last_name";
        List<TeamPlayer> players = jdbcTemplate.query(sql, new RowMapper<TeamPlayer>() {
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
        }, team, team);
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
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members t INNER JOIN team_member_roles r ON t.team = r.team AND t.player = r.player AND r.role = 'Admin' WHERE t.team = ? and t.player = ?)", Boolean.class, team, player);
    }

    private boolean inviteAlreadySent(Integer team, String email) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE team = ? AND email = ? AND role = 'Player')", Boolean.class, team, email);
    }

    private PlayerInvite sendInvite(SqlRowSet team, SqlRowSet admin, String email) {
        SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, p.picture, e.email, u.username FROM player_emails e " + 
                "INNER JOIN players p ON e.player = p.id LEFT OUTER JOIN usernames u ON p.id = u.player WHERE e.email = ?", email);
        if (player.last()) {
            return sendPlayerInvite(team, admin, player);                 
        } else {
            return sendPersonInvite(team, admin, email);
        }        
    }
    
    private PlayerInvite sendPlayerInvite(final SqlRowSet team, final SqlRowSet admin, final SqlRowSet player) {
        final String code = inviteGenerator.generateKey();        
        jdbcTemplate.update("INSERT INTO team_member_invites (team, email, role, code, sent_by, player) VALUES (?, ?, ?, ?, ?, ?)",
                team.getInt("id"), player.getString("email"), TeamRoles.PLAYER, code, admin.getInt("id"), player.getInt("id"));
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(player.getString("email"), player.getString("first_name") + " " + player.getString("last_name")));
               invite.setSubject("Confirm your " + team.getString("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(3, 1);
               model.put("name", player.getString("first_name"));
               model.put("adminUrl", url(team) + playerPath(admin));               
               model.put("admin", new Name(admin.getString("first_name"), admin.getString("last_name")).toString());
               model.put("sport", team.getString("sport"));               
               model.put("teamUrl", url(team));
               model.put("team", team.getString("name"));
               model.put("code", code);
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
         return new PlayerInvite(code, player.getInt("id"), player.getString("first_name"), player.getString("last_name"), player.getString("picture"), player.getString("username"));
    }

    private PlayerInvite sendPersonInvite(final SqlRowSet team, final SqlRowSet admin, final String email) {
        final String code = inviteGenerator.generateKey();        
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
               model.put("code", code);
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
         return new PlayerInvite(code);
    }
    
    private String playerPath(SqlRowSet player) {
        String username = player.getString("username");
        if (username != null) {
            return "/" + username;
        } else {
            return "/" + player.getInt("id");
        }
    }

    private Boolean teamMember(Integer team, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members where team = ? AND player = ?)", Boolean.class, team, player);
    }
    
    private Boolean emailOnFile(String email, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM player_emails where player = ? and email = ?)", Boolean.class, player, email);
    }
    
    // cglib ceremony
    public TeamsController() {}
}
