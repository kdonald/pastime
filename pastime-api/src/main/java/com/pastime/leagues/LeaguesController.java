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
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import com.pastime.util.ErrorBody;
import com.pastime.util.Location;
import com.pastime.util.Name;
import com.pastime.util.PlayerPrincipal;
import com.pastime.util.SecurityContext;
import com.pastime.util.TeamRoles;

@Controller
public class LeaguesController {

    private String siteUrl = "http://localhost:8080";
    
    private String apiUrl = "http://localhost:8081";
    
    private String solrUrl = "http://localhost:8983/solr";
    
    private RestTemplate client;

    private ObjectMapper mapper;
    
    private JdbcTemplate jdbcTemplate;
    
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    
    private String randomPlayersSql;

    private String qualifyingFranchisesSql;

    private String franchiseSql;

    private String playerSearchSql;
    
    private JavaMailSender mailSender;

    private StringTemplateLoader templateLoader;

    private InsecureRandomStringGenerator inviteGenerator = new InsecureRandomStringGenerator(6);

    public LeaguesController(JdbcTemplate jdbcTemplate, JavaMailSender mailSender, StringTemplateLoader templateLoader) {
        initHttpClient();
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.randomPlayersSql = SqlUtils.sql(new ClassPathResource("random-registered-players.sql", getClass()));
        this.franchiseSql = SqlUtils.sql(new ClassPathResource("franchise.sql", getClass()));
        this.qualifyingFranchisesSql = SqlUtils.sql(new ClassPathResource("qualifying-franchises.sql", getClass()));
        this.playerSearchSql = SqlUtils.sql(new ClassPathResource("player-search.sql", getClass()));
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
    }
    
    @RequestMapping(value="/seasons", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> upcomingSeasons() throws URISyntaxException {
        Location location = new Location(28.0674,-80.5595);
        JsonNode json = client.getForObject(new URI(solrUrl + "/select?wt=json&fl=organization_name,organization_link,organization_logo,league_sport,league_format,league_nature," + 
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
        return new ResponseEntity<JsonNode>(upcoming, HttpStatus.OK);
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
                doc.put("organization_logo", siteUrl + "/static/images/organizations/1.png");
                doc.put("league_id", rs.getInt("league_id"));
                doc.put("league_sport", rs.getString("league_sport"));
                doc.put("league_format", rs.getString("league_format"));
                doc.put("league_nature", rs.getString("league_nature"));
                doc.put("season_number", rs.getInt("season_number"));
                doc.put("season_name", rs.getString("season_name"));
                String seasonPathVariable = seasonPathVariable(rs.getInt("season_number"), rs.getString("season_slug"));
                doc.put("season_link", organizationLink + "/" + rs.getString("league_slug") + "/" + seasonPathVariable);                
                doc.put("season_picture", siteUrl + "/static/images/leagues/1/1.png");
                doc.put("season_start_date", new DateTime(rs.getDate("season_start_date"), DateTimeZone.UTC).toString());
                doc.put("venue_id", rs.getInt("venue_id"));
                doc.put("venue_name", rs.getString("venue_name"));
                doc.put("venue_location", new Location(rs.getDouble("venue_latitude"), rs.getDouble("venue_longitude")).toString());
                docs.add(doc);
            }
            private String organizationLink(Integer id, String name) {
                // TODO: we could just always send to /organizations/id and rely on redirect if username is set...
                if (name != null) {
                    return siteUrl + "/" + name;
                } else {
                    return siteUrl + "/organizations/" + id;
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
        client.postForLocation(solrUrl + "/update/json?commit=true", docs);
        return new ResponseEntity<JsonNode>(HttpStatus.CREATED);
    }
    
    @RequestMapping(value="/leagues/{league}/seasons/{season}/sample-players", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> samplePlayers(@PathVariable("league") Integer league, @PathVariable("season") Integer season) {
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
        return new ResponseEntity<JsonNode>(players, HttpStatus.OK);
    }

    @RequestMapping(value="/me", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<? extends Object> me() {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);
        }
        PlayerPrincipal principal = SecurityContext.getPrincipal();
        Map<String, Object> me = new HashMap<String, Object>();
        me.put("id", principal.getId());
        Map<String, Object> links = new HashMap<String, Object>();
        links.put("franchises", apiUrl + "/me/franchises");
        me.put("links", links);
        return new ResponseEntity<Map<String, Object>>(me, HttpStatus.OK);
    }
    
    @RequestMapping(value="/me/franchises", method=RequestMethod.GET, params="league", produces="application/json")
    public ResponseEntity<? extends Object> qualifyingFranchises(@RequestParam("league") Integer league) {
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
        return new ResponseEntity<List<JsonNode>>(franchises, HttpStatus.OK);
    }
    
    @RequestMapping(value="/leagues/{league}/seasons/{season}/teams", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> addTeam(@PathVariable("league") Integer league, @PathVariable("season") Integer season, @RequestParam Map<String, String> team) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }        
        String name = team.get("name");
        if (name == null) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("team name is required"), HttpStatus.BAD_REQUEST);            
        }
        name = name.trim();
        if (name.length() == 0) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("team name can't be empty"), HttpStatus.BAD_REQUEST);            
        }
        Integer franchise = null;
        if (team.containsKey("franchise")) {
            String value = team.get("franchise");
            if (value != null && value.trim().length() > 0) {
                try {
                    franchise = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return new ResponseEntity<ErrorBody>(new ErrorBody("franchise is not a number"), HttpStatus.BAD_REQUEST);                                
                }
            }
        }
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?", league, season); 
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug, franchise) VALUES (?, ?, ?, ?, ?, ?)", league, season, number, name, SlugUtils.toSlug(name), franchise);
        PlayerPrincipal currentPlayer = SecurityContext.getPrincipal();
        Map<String, Object> franchiseInfo = findFranchiseInfo(franchise, currentPlayer.getId());
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", league, season, number, currentPlayer.getId(),
                franchiseInfo.get("number"), franchiseInfo.get("nickname"));
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)", league, season, number, currentPlayer.getId(), TeamRoles.ADMIN);
        
        Map<String, Object> teamData = new HashMap<String, Object>(2, 2);
        teamData.put("number", number);
        URI location = new UriTemplate(apiUrl + "/leagues/{league}/seasons/{season}/teams/{number}").expand(league, season, number);
        Map<String, Object> links = new HashMap<String, Object>(2, 2);
        links.put("players", location + "/players");
        links.put("player_search", location + "/player-search");
        teamData.put("links", links);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<Map<String, Object>>(teamData, headers, HttpStatus.CREATED);
    }

    @RequestMapping(value="/franchises/{id}", method=RequestMethod.GET, produces="application/json")
    @Transactional
    public ResponseEntity<? extends Object> franchise(@PathVariable("id") Integer id) {
        Map<String, Object> franchise = jdbcTemplate.queryForMap(franchiseSql, id);
        String founderLink = playerSiteLink((String) franchise.get("franchise_username"), (Integer) franchise.get("founder_id"));
        if (founderLink != null) {
            franchise.put("founder_link", founderLink);
        }
        franchise.put("founder", extract("founder_", franchise));
        String franchiseLink = franchiseSiteLink((String) franchise.get("username"), (Integer) franchise.get("id"));
        franchise.put("link", franchiseLink);
        List<TeamPlayer> players = Collections.emptyList(); // jdbcTemplate.query(activeFranchisePlayersSql, new TeamPlayerMapper(franchiseLink), id);
        franchise.put("players", players);
        return new ResponseEntity<Map<String, Object>>(franchise, HttpStatus.OK);
    }

    // TODO - filter out players that are already added, mark "me" as "me"
    @RequestMapping(value="/leagues/{league}/seasons/{season}/teams/{team}/player-search", method=RequestMethod.GET, params="name", produces="application/json")
    public ResponseEntity<? extends Object> playerSearch(@PathVariable("league") Integer league, @PathVariable("season") Integer season,
            @PathVariable("team") Integer team, @RequestParam("name") String name) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);
        }
        Map<String, Object> params = new HashMap<String, Object>(1, 1);
        params.put("name", name);
        List<Integer> existing = new ArrayList<Integer>(1);
        existing.add(SecurityContext.getPrincipal().getId());
        params.put("existing", existing);
        List<PlayerThumbnail> players = namedJdbcTemplate.query(playerSearchSql, params, new PlayerThumbnailMapper());
        return new ResponseEntity<List<PlayerThumbnail>>(players, HttpStatus.OK);
    }
    
    @RequestMapping(value="/players/{id}/picture", method=RequestMethod.GET)
    public String playerPicture(@PathVariable("id") Integer id) {
        return "redirect:" + siteUrl + "/static/images/players/18/small.png";        
    }

    @RequestMapping(value="/leagues/{league}/seasons/{season}/teams/{team}/players", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> addPlayer(@PathVariable("league") Integer league, @PathVariable("season") Integer season,
            @PathVariable("team") Integer teamNumber, @RequestParam Map<String, String> playerForm) {
        if (!SecurityContext.authorized()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);            
        }
        Map<String, Object> team = findTeam(league, season, teamNumber);
        if (team == null) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid team id"), HttpStatus.BAD_REQUEST);                        
        }
        if ((Integer)team.get("player_count") >= (Integer)team.get("roster_max")) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("max roster size reached: cannot add anymore players, remove some players first"), HttpStatus.BAD_REQUEST);                                    
        }
        Map<String, Object> admin = DataAccessUtils.singleResult(jdbcTemplate.query("SELECT p.id, p.first_name, p.last_name, t.nickname, t.slug FROM team_member_roles r " + 
                "INNER JOIN team_members t ON r.team = t.team AND r.player = t.player INNER JOIN players p ON t.player = p.id WHERE r.league = ? AND r.season = ? AND r.team = ? AND r.player = ? AND r.role = ?",
                new ColumnMapRowMapper(), league, season, teamNumber, SecurityContext.getPrincipal().getId(), TeamRoles.ADMIN));
        if (admin == null) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("user not an admin for this team"), HttpStatus.FORBIDDEN);            
        }
        if (playerForm.get("id") != null) {
            Integer id = Integer.parseInt(playerForm.get("id"));
            Map<String, Object> player = findPlayer(id);
            if (player == null) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("not a valid player id"), HttpStatus.BAD_REQUEST);                
            }
            if (team.get("gender") != null) {
                if ("m".equals(team.get("gender")) && "f".equals(player.get("gender"))) {
                    return new ResponseEntity<ErrorBody>(new ErrorBody("this team does not allow female players"), HttpStatus.BAD_REQUEST);                    
                } else if ("f".equals(team.get("gender")) && "m".equals(player.get("gender"))) {
                    return new ResponseEntity<ErrorBody>(new ErrorBody("this team does not allow male players"), HttpStatus.BAD_REQUEST);                    
                } else if ("c".equals(team.get("gender"))) {
                    Integer minFemale = (Integer) team.get("roster_min_female");
                    if (minFemale != null) {
                        Integer reservedFemaleSpots = Math.max(0, minFemale - (Integer) team.get("female_player_count"));
                        if (reservedFemaleSpots > 0 && "m".equals(player.get("gender"))) {
                            Integer spotsLeft = (Integer)team.get("roster_max") - (Integer)team.get("player_count");
                            if (reservedFemaleSpots == spotsLeft) {
                                return new ResponseEntity<ErrorBody>(new ErrorBody("all roster spots left are reserved for females"), HttpStatus.BAD_REQUEST);                                
                            }
                        }
                    }
                }
            }
            return addOrInvite(team, admin, player);            
        } else if (playerForm.get("email") != null) {
            return addOrInviteByEmail(team, admin, playerForm);
        } else {
            Map<String, Object> player = findPlayer(SecurityContext.getPrincipal().getId());            
            if (player == null) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("authorized player id is no longer valid"), HttpStatus.CONFLICT);                
            }            
            return addOrInvite(team, admin, player);
        }
    }
    
    private Map<String, Object> findPlayer(Integer id) {
        return DataAccessUtils.singleResult(jdbcTemplate.query("SELECT p.id, p.first_name, p.last_name, e.email FROM players p INNER JOIN player_emails e ON p.id = e.player WHERE p.id = ? AND e.primary_email = true",
                new ColumnMapRowMapper(), id));
    }

    @RequestMapping(value="/error", method={ RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE }, produces="application/json")
    public @ResponseBody ErrorBody error(HttpServletRequest request) {
        String message = (String) request.getAttribute("javax.servlet.error.message");
        if (message != null && message.length() > 0) {
            return new ErrorBody(message);
        } else {
            return null;
        }
    }
    
    // internal helpers
    
    private Map<String, Object> findTeam(Integer league, Integer season, Integer number) {
        return DataAccessUtils.singleResult(jdbcTemplate.query("SELECT t.league, t.season, t.number, t.name, t.slug, t.franchise FROM teams t WHERE t.league = ? AND t.season = ? AND t.number = ?", new ColumnMapRowMapper(), league, season, number));        
    }

    private ResponseEntity<? extends Object> addOrInviteByEmail(Map<String, Object> team, Map<String, Object> admin, Map<String, String> playerForm) {
        Map<String, Object> player = findPlayerByEmail((String) playerForm.get("email"));
        if (player != null) {
            return addOrInvite(team, admin, player);
        } else {
            return new ResponseEntity<InvitedPlayer>(sendPersonInvite(team, admin, playerForm), HttpStatus.CREATED);
        }
    }
    
    private Map<String, Object> findPlayerByEmail(String string) {
        // TODO
        return null;
    }

    private ResponseEntity<? extends Object> addOrInvite(Map<String, Object> team, Map<String, Object> admin, Map<String, Object> player) {
        if (player.get("id").equals(admin.get("id"))) {
            return new ResponseEntity<TeamPlayer>(addPlayer(team, (Integer) player.get("id")), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<InvitedPlayer>(sendPlayerInvite(team, admin, player), HttpStatus.CREATED);        
        }
    }

    private TeamPlayer addPlayer(Map<String, Object> team, Integer playerId) {
        if (!teamMember(team, playerId)) {
            // the player isn't already a team member, make him or her one            
            Map<String, Object> franchiseInfo = findFranchiseInfo((Integer) team.get("Franchise"), playerId);            
            jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", team.get("league"), team.get("season"), team.get("number"), playerId,
                    franchiseInfo.get("number"), franchiseInfo.get("nickname"));                    
        }
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role, player_status) VALUES (?, ?, ?, ?, ?, ?)",
                team.get("league"), team.get("season"), team.get("number"), playerId, TeamRoles.PLAYER, 'a');
        // TODO
        return null;
    }
    
    private String url(Map<String, Object> team) {
        return siteUrl + "/leagues/" + team.get("league") + "/seasons/" + team.get("season") + "/teams/" + team.get("number");
    }
    
    private InvitedPlayer sendPlayerInvite(final Map<String, Object> team, final Map<String, Object> admin, final Map<String, Object> player) {
        final String code = inviteGenerator.generateKey();        
        jdbcTemplate.update("INSERT INTO team_member_invites (league, season, team, email, role, code, sent_by, player) VALUES (?, ?, ?, ?, ?, ?)",
                team.get("league"), team.get("season"), team.get("number"), player.get("email"), TeamRoles.PLAYER, code, admin.get("id"), player.get("id"));
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress((String)player.get("email"), player.get("first_name") + " " + player.get("last_name")));
               invite.setSubject("Confirm your " + team.get("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(7, 1);
               model.put("name", player.get("first_name"));
               //model.put("adminUrl", url(team) + playerPath(admin));               
               model.put("admin", new Name((String)admin.get("first_name"), (String)admin.get("last_name")).toString());
               model.put("sport", team.get("sport"));               
               model.put("teamUrl", url(team));
               model.put("team", team.get("name"));
               model.put("code", code);
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
         return null;
         //return new InvitedPlayer(code, sent, (String) player.get("email"), resendLink, (String) player.get("first_name"), (String) player.get("last_name"), link, picture);
    }

    private InvitedPlayer sendPersonInvite(final Map<String, Object> team, final Map<String, Object> admin, final Map<String, String> playerForm) {
        final String code = inviteGenerator.generateKey();
        jdbcTemplate.update("INSERT INTO team_member_invites (league, season, team, email, role, code, sent_by) VALUES (?, ?, ?, ?, ?, ?, ?)",
                team.get("league"), team.get("season"), team.get("number"), playerForm.get("email"), TeamRoles.PLAYER, code, admin.get("id"));        
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               if (playerForm.get("name") != null) {
                   invite.setTo(new InternetAddress(playerForm.get("email"), playerForm.get("name")));                   
               } else {
                   invite.setTo(new InternetAddress(playerForm.get("email")));                   
               }
               invite.setSubject("Confirm your " + team.get("name") + " team membership.");
               Map<String, Object> model = new HashMap<String, Object>(7, 1);
               if (playerForm.get("name") != null) {
                   model.put("name", playerForm.get("name"));                   
               } else {
                   model.put("name", "Hello");                   
               }
               //model.put("adminUrl", url(team) + playerPath(admin));               
               model.put("admin", new Name((String)admin.get("first_name"), (String)admin.get("last_name")).toString());
               model.put("sport", (String)team.get("sport"));
               model.put("teamUrl", url(team));
               model.put("team", (String)team.get("name"));               
               model.put("code", code);
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
         return null;
         // return new InvitedPlayer(code, sent, playerForm.get("email"), resendLink, playerForm.get("name"));
    }
    
    private Boolean teamMember(Map<String, Object> team, Integer player) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members where league = ? AND season = ? AND team = ? AND player = ?)", Boolean.class, 
                team.get("league"), team.get("season"), team.get("number"), player);
    }
    
    private Map<String, Object> findFranchiseInfo(Integer franchise, Integer playerId) {
        if (franchise == null) {
            return Collections.emptyMap();
        }
        return jdbcTemplate.queryForMap("SELECT number, nickname FROM franchise_members WHERE franchise = ? AND player = ?", franchise, playerId);
    }
    
    public class PlayerThumbnailMapper implements RowMapper<PlayerThumbnail> {

        @Override
        public PlayerThumbnail mapRow(ResultSet rs, int rowNum) throws SQLException {
            String picture = playerApiLink(rs.getInt("id")) + "/picture";
            return new PlayerThumbnail(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"), siteLink(rs), picture);
        }
        
        private String siteLink(ResultSet rs) throws SQLException {
            return playerSiteLink(rs.getString("username"), rs.getInt("id"));                
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
    
    private String franchiseSiteLink(String username, Integer id) {
        return siteLink("franchises", username, id);
    }
    
    private String playerSiteLink(String username, Integer id) {
        return siteLink("players", username, id);
    }
    
    private String playerApiLink(Integer id) {
        return apiLink("players", id);
    }
    
    private String siteLink(String type, String username, Integer id) {
        if (username != null) {
            return siteUrl + "/" + username;
        } else if (id != null) {
            return siteUrl + "/" + type + "/" + id;
        } else {
            return siteUrl + "/" + type;
        }
    }
    
    private String apiLink(String type, Integer id) {
        if (id != null) {
            return apiUrl + "/" + type + "/" + id;
        } else {
            return apiUrl + "/" + type;
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