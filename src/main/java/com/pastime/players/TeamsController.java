package com.pastime.players;

import static org.springframework.jdbc.core.SqlStatements.use;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
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
    
    private String franchiseSql;
    
    private String activeFranchisePlayersSql;
    
    public TeamsController(JdbcTemplate jdbcTemplate, JavaMailSender mailSender, StringTemplateLoader templateLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
        this.franchiseSql = SqlUtils.sql(new ClassPathResource("franchise.sql", getClass()));
        this.activeFranchisePlayersSql = SqlUtils.sql(new ClassPathResource("franchise-players-active.sql", getClass()));
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
        Integer teamId = (Integer) use(jdbcTemplate).insert("INSERT INTO franchises (name, sport) VALUES (?, ?)", teamForm.getName(), teamForm.getSport());
        Player player = SecurityContext.getCurrentPlayer();
        jdbcTemplate.update("INSERT INTO franchise_members (franchise, player) VALUES (?, ?)", teamId, player.getId());
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (?, ?, ?)", teamId, player.getId(), TeamRoles.ADMIN);
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
    
    @RequestMapping(value="/franchises/{id}", method=RequestMethod.GET, produces="application/json")
    @Transactional
    public ResponseEntity<? extends Object> teamData(@PathVariable Integer id) {
        Map<String, Object> franchise = jdbcTemplate.queryForMap(franchiseSql, id);
        String founderUsername = (String) franchise.get("franchise_username");
        if (founderUsername != null) {
            franchise.put("founder_link", "http://pastime.com/" + founderUsername);
        } else if (franchise.get("founder_id") != null) {
            franchise.put("founder_link", "http:/pastime.com/players/" + franchise.get("founder_id"));
        }
        franchise.put("founder", extract("founder_", franchise));
        if (franchise.get("username") != null) {
            franchise.put("link", "http://pastime.com/" + franchise.get("username"));
        } else {
            franchise.put("link", "http://pastime.com/franchises/" + franchise.get("id"));
        }
        List<Map<String, Object>> players = jdbcTemplate.queryForList(activeFranchisePlayersSql, id);
        franchise.put("players", players);
        return new ResponseEntity<Map<String, Object>>(franchise, HttpStatus.OK);
    }
    
    @RequestMapping(value="/teams/{teamId}/players", method=RequestMethod.POST, produces="application/json")
    @Transactional    
    public ResponseEntity<? extends Object> addPlayer(@PathVariable Integer teamId, PlayerForm form) {
        if (!SecurityContext.playerSignedIn()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }
        if (form.getId() != null && form.getEmail() == null || form.getEmail().length() == 0) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("no player id or email address provided"), HttpStatus.BAD_REQUEST);            
        }
        SqlRowSet team = findTeam(teamId);
        if (!team.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid team id"), HttpStatus.BAD_REQUEST);                        
        }        
        SqlRowSet admin = jdbcTemplate.queryForRowSet("select p.id, p.first_name, p.last_name, t.nickname, t.slug FROM franchise_member_roles r " + 
                "INNER JOIN franchise_members t ON r.franchise = t.franchise AND r.player = t.player INNER JOIN players p ON t.player = p.id WHERE r.franchise = ? and r.player = ? AND r.role = 'Admin'",
                team.getInt("id"), SecurityContext.getCurrentPlayer().getId());
        if (!admin.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("you're not an admin for this team"), HttpStatus.FORBIDDEN);            
        }        
        if (form.getEmail() != null) {
            SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, e.email, u.username FROM player_emails e " + 
                    "INNER JOIN players p ON e.player = p.id LEFT OUTER JOIN usernames u ON p.id = u.player WHERE e.email = ?", form.getEmail());
            if (player.last()) {
                return addOrInvite(team, admin, player, form.getEmail());
            } else {
                return invite(team, admin, player, form.getEmail());                
            }
        } else {
            SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, e.email FROM players p INNER JOIN player_emails e ON p.id = e.player WHERE p.id = ? AND e.primary_email = true", form.getEmail());
            if (!player.last()) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid player id"), HttpStatus.BAD_REQUEST);                
            }
            return addOrInvite(team, admin, player, form.getEmail());            
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
        SqlRowSet invite = jdbcTemplate.queryForRowSet("SELECT email, role, player, accepted, accepted_player FROM franchise_member_invites WHERE franchise = ? AND code = ?", teamId, code);
        if (!invite.last()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Boolean accepted = invite.getBoolean("accepted");
        if (!invite.wasNull()) {
            if (accepted) {
                return "redirect:/teams/" + teamId + "/" + invite.getInt("accepted_player");
            } else {
                model.addAttribute("answer", accepted);
                return "teams/invite-already-answered";                
            }
        }
        a = a.toLowerCase();
        if (a.equals("accept")) {
            Integer playerId = invite.getInt("player");
            if (invite.wasNull()) {
                // no pastime user associated with email address
                // TODO--this needs to be revisited... we should probably render a view here that:
                // a. if no user is signed in, allows the user to sign-up if they are new 
                // b. if no user is signed in, allows the user to sign-in if they already have an account (the email the invite was sent to can then be added to this account)
                // c. if a user is signed in, allows them to add this email to that account, sign-out & sign-in to another account, or sign-out & create a new account
                Player currentPlayer = SecurityContext.getCurrentPlayer();
                if (currentPlayer == null) {
                    // nobody is signed-in, assume a completely new user
                    return "redirect:/signup?email=" + invite.getString("email");
                }
                playerId = currentPlayer.getId();
                if (!emailOnFile(invite.getString("email"), playerId)) {
                  // somebody else is signed-in, assume a existing user under a different address
                  return "redirect:/signin?email=" + invite.getString("email");
                }
            }
            String role = invite.getString("role");
            if (role.equals(TeamRoles.PLAYER)) {
                addPlayer(teamId, playerId);
                Date answered = new Date();
                jdbcTemplate.update("UPDATE franchise_member_invites set accepted = true, accepted_player = ?, answered_on = ? where franchise = ? and code = ?", playerId, answered, teamId, code);
                return "redirect:/teams/" + teamId + "/" + playerId;
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

    private void addPlayer(Integer teamId, Integer playerId) {
        if (!teamMember(teamId, playerId)) {
            // the player isn't already a team member, make him or her one
            jdbcTemplate.update("INSERT INTO franchise_members (franchise, player) VALUES (?, ?)", teamId, playerId);                    
        }
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role, player_status) VALUES (?, ?, 'Player', 'a')", teamId, playerId);
    }

    @RequestMapping(value="/teams/{teamId}/invites/{code}", method={ RequestMethod.POST, RequestMethod.DELETE })
    @Transactional
    public ResponseEntity<? extends Object> declineInvite(@PathVariable Integer teamId, @PathVariable String code) {
        SqlRowSet invite = jdbcTemplate.queryForRowSet("SELECT accepted FROM franchise_member_invites WHERE franchise = ? AND code = ?", teamId, code);
        if (!invite.last()) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
        invite.getBoolean("accepted");
        if (!invite.wasNull()) {
            return new ResponseEntity<String>("Invite already answered", HttpStatus.CONFLICT);
        }        
        jdbcTemplate.update("UPDATE franchise_member_invites set accepted = false, answered_on = ? where franchise = ? and code = ?", new Date(), teamId, code);        
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value="/teams/{teamId}/{memberKey}", method=RequestMethod.GET)
    public String member(@PathVariable Integer teamId, @PathVariable String memberKey, Model model, HttpServletResponse response) throws IOException {
        Integer memberId = parseInteger(memberKey);
        SqlRowSet member;
        if (memberId != null) {
            member = jdbcTemplate.queryForRowSet("SELECT number, nickname FROM franchise_members WHERE franchise = ? AND player = ?", teamId, memberId);
        } else {
            member = jdbcTemplate.queryForRowSet("SELECT number, nickname FROM franchise_members WHERE franchise = ? AND slug = ?", teamId, memberKey);
        }
        if (!member.last()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("number", member.getObject("number"));
        model.addAttribute("nickname", member.getString("nickname"));        
        return "teams/player";
    }

    @RequestMapping(value="/teams/{teamId}/{memberKey}", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> member(@PathVariable Integer teamId, @PathVariable String memberKey, @Valid TeamMemberForm form, Model model) {
        Player currentPlayer = SecurityContext.getCurrentPlayer();
        if (currentPlayer == null) {
            return new ResponseEntity<Object>(HttpStatus.FORBIDDEN);            
        }
        Integer memberId = parseInteger(memberKey);
        if (memberId != null) {
            if (!teamMember(teamId, memberId)) {
                return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);                
            }            
        } else {
            String username = memberKey;
            SqlRowSet member = jdbcTemplate.queryForRowSet("SELECT player FROM franchise_members WHERE franchise = ? AND slug = ?", teamId, username);
            if (!member.last()) {
                return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);                
            }
            memberId = member.getInt("player");
        }
        if (!currentPlayer.getId().equals(memberId)) {
            return new ResponseEntity<Object>(HttpStatus.FORBIDDEN);                
        }
        jdbcTemplate.update("UPDATE franchise_members SET number = ?, nickname = ? WHERE franchise = ? AND player = ?", form.getNumber(), form.getNickname(), teamId, memberId);        
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }
    
    // internal helpers
    
    private SqlRowSet findTeam(Integer id) {
        return jdbcTemplate.queryForRowSet("SELECT t.id, t.name, t.sport, u.username FROM franchises t LEFT OUTER JOIN usernames u on t.id = u.franchise WHERE t.id = ?", id);        
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
        return "http://pastime.com/static/images/default-team-logo.png";
    }

    private void addPlayers(Integer team, Model model) {
        final String teamUrl = (String) model.asMap().get("url");
        String sql = "SELECT p.id, p.first_name, p.last_name, t.number, t.nickname, t.slug, r.player_status, r.player_captain, r.player_captain_of FROM franchise_members t " + 
                "INNER JOIN franchise_member_roles r ON t.franchise = r.franchise AND t.player = r.player INNER JOIN players p on t.player = p.id WHERE r.franchise = ? AND r.role = 'Player' ORDER BY p.last_name";
        List<TeamPlayer> players = jdbcTemplate.query(sql, new RowMapper<TeamPlayer>() {
            public TeamPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TeamPlayer(teamUrl, rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), getPicture(rs), rs.getInt("number"), rs.getString("nickname"));
            }
            private String getPicture(ResultSet rs) throws SQLException {
                return null;
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
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM franchise_members t INNER JOIN franchise_member_roles r ON t.franchise = r.franchise AND t.player = r.player AND r.role = 'Admin' WHERE t.franchise = ? and t.player = ?)", Boolean.class, team, player);
    }

    private boolean inviteAlreadySent(Integer team, String email) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM franchise_member_invites WHERE franchise = ? AND email = ? AND role = 'Player')", Boolean.class, team, email);
    }

    private ResponseEntity<? extends Object> addOrInvite(SqlRowSet team, SqlRowSet admin, SqlRowSet player, String email) {
        if (player.getInt("id") == admin.getInt("id")) {
            // no invite needed for adding yourself
            addPlayer(team.getInt("id"), player.getInt("id"));
            return new ResponseEntity<Object>(HttpStatus.CREATED);
        } else {
            return invite(team, admin, player, email);
        }
    }

    private ResponseEntity<? extends Object> invite(SqlRowSet team, SqlRowSet admin, SqlRowSet player, String email) {
        if (inviteAlreadySent(team.getInt("id"), email)) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("player invite already sent: POST to " + url(team) + "/invites/{code} if you wish to resend"), HttpStatus.CONFLICT);                    
        }
        PlayerInvite invite;
        if (player.last()) {
            invite = sendPlayerInvite(team, admin, player);
        } else {
            invite = sendPersonInvite(team, admin, email);
        }
        return new ResponseEntity<PlayerInvite>(invite, HttpStatus.CREATED);        
    }
    
    private PlayerInvite sendPlayerInvite(final SqlRowSet team, final SqlRowSet admin, final SqlRowSet player) {
        final String code = inviteGenerator.generateKey();        
        jdbcTemplate.update("INSERT INTO franchise_member_invites (franchise, email, role, code, sent_by, player) VALUES (?, ?, ?, ?, ?, ?)",
                team.getInt("id"), player.getString("email"), TeamRoles.PLAYER, code, admin.getInt("id"), player.getInt("id"));
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(player.getString("email"), player.getString("first_name") + " " + player.getString("last_name")));
               invite.setSubject("Confirm your " + team.getString("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(7, 1);
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
         return new PlayerInvite(code, player.getInt("id"), player.getString("first_name"), player.getString("last_name"), null, player.getString("username"));
    }

    private PlayerInvite sendPersonInvite(final SqlRowSet team, final SqlRowSet admin, final String email) {
        final String code = inviteGenerator.generateKey();
        jdbcTemplate.update("INSERT INTO franchise_member_invites (franchise, email, role, code, sent_by) VALUES (?, ?, ?, ?, ?)",
                team.getInt("id"), email, TeamRoles.PLAYER, code, admin.getInt("id"));        
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(email));
               invite.setSubject("Confirm your " + team.getString("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(7, 1);
               model.put("name", "Hello");
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
         return new PlayerInvite(code);
    }
    
    private String playerPath(SqlRowSet player) {
        String username = player.getString("slug");
        if (username != null) {
            return "/" + username;
        } else {
            return "/" + player.getInt("id");
        }
    }

    private Boolean teamMember(Integer team, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM franchise_members where franchise = ? AND player = ?)", Boolean.class, team, player);
    }
    
    private Boolean emailOnFile(String email, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM player_emails where player = ? and email = ?)", Boolean.class, player, email);
    }
    
    private static Integer parseInteger(String string) {
        try { 
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            return null;
        }        
    }

    private Map<String, Object> extract(String prefix, Map<String, Object> map) {
        Map<String, Object> extracted = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        boolean notNull = false;
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            int index = key.indexOf(prefix);
            if (index != -1) {
                Object value = entry.getValue();
                if (value != null) {
                    notNull = true;
                }
                extracted.put(key.substring(index + prefix.length()), value);
                it.remove();                
            }
        }
        return notNull ? extracted : null;
    }
    
    // cglib ceremony
    public TeamsController() {}
}
