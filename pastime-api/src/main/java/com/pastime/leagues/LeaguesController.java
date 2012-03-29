package com.pastime.leagues;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
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
import org.springframework.util.SlugUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.pastime.util.ErrorBody;
import com.pastime.util.Location;
import com.pastime.util.Name;
import com.pastime.util.PlayerInvite;
import com.pastime.util.PlayerPrincipal;
import com.pastime.util.SecurityContext;
import com.pastime.util.TeamRoles;

@Controller
public class LeaguesController {

    private RestTemplate client;

    private ObjectMapper mapper;
    
    private JdbcTemplate jdbcTemplate;
    
    private String randomPlayersSql;

    private String qualifyingFranchisesSql;

    private String franchiseSql;

    private String activeFranchisePlayersSql;
    
    private String playerSearchSql;
    
    private JavaMailSender mailSender;

    private StringTemplateLoader templateLoader;

    private InsecureRandomStringGenerator inviteGenerator = new InsecureRandomStringGenerator(6);

    public LeaguesController(JdbcTemplate jdbcTemplate, JavaMailSender mailSender, StringTemplateLoader templateLoader) {
        initHttpClient();
        this.jdbcTemplate = jdbcTemplate;
        this.randomPlayersSql = SqlUtils.sql(new ClassPathResource("random-registered-players.sql", getClass()));
        this.franchiseSql = SqlUtils.sql(new ClassPathResource("franchise.sql", getClass()));
        this.activeFranchisePlayersSql = SqlUtils.sql(new ClassPathResource("franchise-players-active.sql", getClass()));        
        this.qualifyingFranchisesSql = SqlUtils.sql(new ClassPathResource("qualifying-franchises.sql", getClass()));
        this.playerSearchSql = SqlUtils.sql(new ClassPathResource("player-search.sql", getClass()));
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
    }
    
    @RequestMapping(value="/seasons", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> upcomingSeasons() throws URISyntaxException {
        Location location = new Location(28.0674,-80.5595);
        JsonNode json = client.getForObject(new URI("http://localhost:8983/solr/select?wt=json&fl=organization_name,organization_link,organization_logo,league_sport,league_format,league_nature," + 
                "season_name,season_link,season_start_date,season_picture,venue_name&q=*:*&fq=%7B!geofilt%7D&sfield=venue_location&pt=" + location + "&d=25"), JsonNode.class);
        JsonNode docs = json.get("response").get("docs");
        ArrayNode upcoming = mapper.createArrayNode();
        for (JsonNode doc : docs) {
            ObjectNode season = mapper.createObjectNode();
            
            season.put("name", doc.get("season_name").asText());
            season.put("picture", doc.get("season_picture").asText());
            season.put("start_date", doc.get("season_start_date").asText());
            season.put("link", doc.get("season_link").asText());

            ObjectNode organization = mapper.createObjectNode();
            organization.put("name", doc.get("organization_name").asText());
            organization.put("link", doc.get("organization_link").asText());
            organization.put("logo", doc.get("organization_logo").asText());
            season.put("organization", organization);
            
            ObjectNode league = mapper.createObjectNode();
            league.put("sport", doc.get("league_sport").asText());
            league.put("format", doc.get("league_format").asText());
            league.put("nature", doc.get("league_nature") != null ? doc.get("league_nature").asText() : null);
            season.put("league", league);

            ObjectNode venue = mapper.createObjectNode();
            venue.put("name", doc.get("venue_name").asText());
            season.put("venue", venue);
            
            upcoming.add(season);
        }
        return new ResponseEntity<JsonNode>(upcoming, HttpStatus.ACCEPTED);
    }

    // TODO "/batch/*" is temp & doesn't belong as part of the API
    
    @RequestMapping(value="/batch/seasons", method=RequestMethod.POST, produces="application/json")
    public ResponseEntity<? extends Object> indexUpcomingLeagues() throws URISyntaxException {
        String upcomingSql = SqlUtils.sql(new ClassPathResource("upcoming-seasons.sql", getClass()));        
        final ArrayNode docs = mapper.createArrayNode();
        jdbcTemplate.query(upcomingSql, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                ObjectNode doc = mapper.createObjectNode();
                Integer organizationId = rs.getInt("organization_id");
                doc.put("id", "upcoming_seasons:" + rs.getInt("league_id") + ":" + rs.getInt("season_number"));
                doc.put("organization_id", organizationId);                
                doc.put("organization_name", rs.getString("organization_name"));
                String organizationLink = organizationLink(organizationId, rs.getString("organization_username"));
                doc.put("organization_link", organizationLink);
                doc.put("organization_logo", "http://pastime.com/static/images/organizations/1.png");
                doc.put("league_id", rs.getInt("league_id"));
                doc.put("league_sport", rs.getString("league_sport"));
                doc.put("league_format", rs.getString("league_format"));
                doc.put("league_nature", rs.getString("league_nature"));
                doc.put("season_number", rs.getInt("season_number"));
                doc.put("season_name", rs.getString("season_name"));
                String seasonPathVariable = seasonPathVariable(rs.getInt("season_number"), rs.getString("season_slug"));
                doc.put("season_link", organizationLink + "/" + rs.getString("league_slug") + "/" + seasonPathVariable);                
                doc.put("season_picture", "http://pastime.com/static/images/leagues/1/1.png");
                doc.put("season_start_date", new DateTime(rs.getDate("season_start_date"), DateTimeZone.UTC).toString());
                doc.put("venue_id", rs.getInt("venue_id"));
                doc.put("venue_name", rs.getString("venue_name"));
                doc.put("venue_location", new Location(rs.getDouble("venue_latitude"), rs.getDouble("venue_longitude")).toString());
                docs.add(doc);
            }
            private String organizationLink(Integer id, String name) {
                // TODO: we could just always send to /organizations/id and rely on redirect if username is set...
                if (name != null) {
                    return "http://pastime.com/" + name;
                } else {
                    return "http://pastime.com/organizations/" + id;
                }
            }
            private String seasonPathVariable(Integer number, String slug) {
                if (slug != null && slug.length() > 0) {
                    return slug;
                } else {
                    return number.toString();
                }
            }
        });
        client.postForLocation("http://localhost:8983/solr/update/json?commit=true", docs);
        return new ResponseEntity<JsonNode>(HttpStatus.CREATED);
    }
    
    @RequestMapping(value="/leagues/{league}/seasons/{season}/sample-players", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> samplePlayers(@PathVariable Integer league, @PathVariable Integer season) {
        final ArrayNode players = mapper.createArrayNode();
        jdbcTemplate.query(randomPlayersSql, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                ObjectNode player = mapper.createObjectNode();
                player.put("id", rs.getInt("id"));
                player.put("slug", rs.getString("slug"));
                player.put("number", rs.getInt("number"));
                player.put("nickname", rs.getString("nickname"));
                players.add(player);
            }
        }, league, season);        
        return new ResponseEntity<JsonNode>(players, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value="/me/franchises", method=RequestMethod.GET, params="league", produces="application/json")
    public ResponseEntity<? extends Object> qualifyingFranchises(@RequestParam Integer league) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);
        }
        List<JsonNode> franchises = jdbcTemplate.query(qualifyingFranchisesSql, new RowMapper<JsonNode>() {
            @Override
            public JsonNode mapRow(ResultSet rs, int rowNum) throws SQLException {
                ObjectNode franchise = mapper.createObjectNode();
                franchise.put("id", rs.getInt("id"));
                franchise.put("name", rs.getString("name"));                
                return franchise;
            }
        }, SecurityContext.getPrincipal().getId(), league);
        return new ResponseEntity<List<JsonNode>>(franchises, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/leagues/{league}/seasons/{season}/teams", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> addTeam(@PathVariable Integer league, @PathVariable Integer season, @RequestParam Map<String, Object> team) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }        
        String name = (String) team.get("name");
        if (name == null) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("team name is required"), HttpStatus.BAD_REQUEST);            
        }
        name = name.trim();
        if (name.length() == 0) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("team name can't be empty"), HttpStatus.BAD_REQUEST);            
        }
        Integer franchise = null;
        if (team.containsKey("franchise")) {
            String value = (String) team.get("franchise");
            if (value != null && value.trim().length() > 0) {
                try {
                    franchise = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return new ResponseEntity<ErrorBody>(new ErrorBody("franchise is not a number"), HttpStatus.BAD_REQUEST);                                
                }
            }
        }
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?", league, season); 
        jdbcTemplate.update("INSERT INTO teams (league, season, team, name, slug, franchise) VALUES (?, ?, ?, ?, ?)", league, season, number, name, SlugUtils.toSlug(name), franchise);
        PlayerPrincipal currentPlayer = SecurityContext.getPrincipal();
        Map<String, Object> franchiseInfo = findFranchiseInfo(franchise, currentPlayer.getId());
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", league, season, number, currentPlayer.getId(),
                franchiseInfo.get("number"), franchiseInfo.get("nickname"));
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)", league, season, number, currentPlayer.getId(), TeamRoles.ADMIN);                
        return new ResponseEntity<Integer>(number, HttpStatus.CREATED);
    }

    @RequestMapping(value="/franchises/{id}", method=RequestMethod.GET, produces="application/json")
    @Transactional
    public ResponseEntity<? extends Object> franchise(@PathVariable Integer id) {
        Map<String, Object> franchise = jdbcTemplate.queryForMap(franchiseSql, id);
        String founderLink = playerLink((String) franchise.get("franchise_username"), (Integer) franchise.get("founder_id"));
        if (founderLink != null) {
            franchise.put("founder_link", founderLink);
        }
        franchise.put("founder", extract("founder_", franchise));
        String franchiseLink = franchiseLink((String) franchise.get("username"), (Integer) franchise.get("id"));
        franchise.put("link", franchiseLink);
        List<Player> players = jdbcTemplate.query(activeFranchisePlayersSql, new PlayerMapper(franchiseLink), id);
        franchise.put("players", players);
        return new ResponseEntity<Map<String, Object>>(franchise, HttpStatus.OK);
    }
        
    @RequestMapping(value="/players", method=RequestMethod.GET, params="name", produces="application/json")
    // TODO exclude "me" and existing franchise players from search on server or client?
    public ResponseEntity<? extends Object> playerSearch(@RequestParam String name, @RequestParam Integer franchise) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);
        }
        List<Player> players = jdbcTemplate.query(playerSearchSql, new PlayerMapper(null), name + "%");
        return new ResponseEntity<List<Player>>(players, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/{username}/picture", method=RequestMethod.GET)
    public String picture() {
        return "redirect:http://pastime.com/static/images/players/18/small.png";        
    }

    @RequestMapping(value="/leagues/{league}/seasons/{season}/teams/{team}/players", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> addPlayer(@PathVariable Integer league, @PathVariable Integer season, @PathVariable Integer teamNumber, @RequestParam Map<String, Object> playerForm) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }
        if (playerForm.get("id") == null && playerForm.get("email") == null || ((String) playerForm.get("email")).length() == 0) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("neither a player id or email address is provided"), HttpStatus.BAD_REQUEST);            
        }
        SqlRowSet team = findTeam(league, season, teamNumber);
        if (!team.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid team id"), HttpStatus.BAD_REQUEST);                        
        }         
        SqlRowSet admin = jdbcTemplate.queryForRowSet("select p.id, p.first_name, p.last_name, t.nickname, t.slug FROM team_member_roles r " + 
                "INNER JOIN team_members t ON r.team = t.team AND r.player = t.player INNER JOIN players p ON t.player = p.id WHERE r.team = ? and r.player = ? AND r.role = ?",
                teamNumber, SecurityContext.getPrincipal().getId(), TeamRoles.ADMIN);
        if (!admin.last()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("you're not an admin for this team"), HttpStatus.FORBIDDEN);            
        }
        if (playerForm.get("id") != null) {
            Integer id = (Integer) playerForm.get("id");
            SqlRowSet player = jdbcTemplate.queryForRowSet("SELECT p.id, p.first_name, p.last_name, e.email FROM players p INNER JOIN player_emails e ON p.id = e.player WHERE p.id = ? AND e.primary_email = true", id);
            if (!player.last()) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid player id"), HttpStatus.BAD_REQUEST);                
            }
            return addOrInvite(team, admin, player, player.getString("email"));            
        }
        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }
    
    private SqlRowSet findTeam(Integer league, Integer season, Integer number) {
        return jdbcTemplate.queryForRowSet("SELECT t.league, t.season, t.number, t.name, t.slug, t.franchise FROM teams t WHERE t.league = ? AND t.season = ? AND t.number = ?", league, season, number);        
    }
    
    private ResponseEntity<? extends Object> addOrInvite(SqlRowSet team, SqlRowSet admin, SqlRowSet player, String email) {
        if (player.getInt("id") == admin.getInt("id")) {
            // no invite needed for adding yourself
            addPlayer(team, player.getInt("id"));
            return new ResponseEntity<Object>(HttpStatus.CREATED);
        } else {
            return invite(team, admin, player, email);
        }
    }
    
    private void addPlayer(SqlRowSet team, Integer playerId) {
        if (!teamMember(team, playerId)) {
            // the player isn't already a team member, make him or her one            
            Map<String, Object> franchiseInfo = findFranchiseInfo(team.getInt("Franchise"), playerId);            
            jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", team.getInt("league"), team.getInt("season"), team.getInt("number"), playerId,
                    franchiseInfo.get("number"), franchiseInfo.get("nickname"));                    
        }
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role, player_status) VALUES (?, ?, ?, ?, ?, ?)",
                team.getInt("league"), team.getInt("season"), team.getInt("number"), playerId, TeamRoles.PLAYER, 'a');
    }
    
    private ResponseEntity<? extends Object> invite(SqlRowSet team, SqlRowSet admin, SqlRowSet player, String email) {
        if (inviteAlreadySent(team, email)) {
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

    private String url(SqlRowSet team) {
        return "http://pastime.com/leagues/" + team.getInt("league") + "/seasons/" + team.getInt("season") + "/teams/" + team.getString("id");
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
               //model.put("adminUrl", url(team) + playerPath(admin));               
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
               //model.put("adminUrl", url(team) + playerPath(admin));               
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
    private boolean inviteAlreadySent(SqlRowSet team, String email) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE league = ? AND season = ? AND team = ? AND email = ? AND role = 'Player')", Boolean.class, team, email);
    }

    private Boolean teamMember(SqlRowSet team, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members where league = ? AND season = ? AND team = ? AND player = ?)", Boolean.class, 
                team.getInt("league"), team.getInt("season"), team.getInt("number"), player);
    }
    
    private Map<String, Object> findFranchiseInfo(Integer franchise, Integer playerId) {
        if (franchise == null) {
            return Collections.emptyMap();
        }
        return jdbcTemplate.queryForMap("SELECT number, nickname FROM franchise_members WHERE franchise = ? AND player = ?", franchise, playerId);
    }
    
    public static class PlayerMapper implements RowMapper<Player> {

        private String teamLink;
        
        public PlayerMapper(String teamLink) {
            this.teamLink = teamLink;
        }

        @Override
        public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Player(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getInt("number"), rs.getString("nickname"), link(rs));
        }
        
        private String link(ResultSet rs) throws SQLException {
            if (teamLink != null) {
                Object playerPath = rs.getString("slug");
                if (playerPath == null) {
                    playerPath = rs.getInt("id");
                }
                return teamLink + "/" + playerPath;
            } else {
                return playerLink(rs.getString("username"), rs.getInt("id"));                
            }
        }
        
    }

    // internal helpers
    
    private void initHttpClient() {
        client = new RestTemplate();
        client.setRequestFactory(new HttpComponentsClientHttpRequestFactory());        
        
        mapper = new ObjectMapper();
        MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
        converter.setObjectMapper(mapper);
        
        List<MediaType> mediaTypes = new ArrayList<MediaType>(2);
        mediaTypes.add(MediaType.APPLICATION_JSON);
        // Solr returns text/plain responses for JSON results
        mediaTypes.add(MediaType.TEXT_PLAIN);
        converter.setSupportedMediaTypes(mediaTypes);

        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(1);        
        converters.add(converter);        
        client.setMessageConverters(converters);        
    }
    
    private static String franchiseLink(String username, Integer id) {
        return link("franchises", username, id);
    }
    
    private static String playerLink(String username, Integer id) {
        return link("players", username, id);
    }
    
    private static String link(String type, String username, Integer id) {
        if (username != null) {
            return "http://pastime.com/" + username;
        } else if (id != null) {
            return "http:/pastime.com/" + type + "/" + id;
        } else {
            return null;
        }
    }
    
    private static Map<String, Object> extract(String prefix, Map<String, Object> map) {
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
    
    // cglib boilerplate
    public LeaguesController() {
        
    }
}