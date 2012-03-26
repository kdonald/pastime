package com.pastime.leagues;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.pastime.players.ErrorBody;
import com.pastime.players.PlayerPrincipal;
import com.pastime.players.SecurityContext;
import com.pastime.players.TeamRoles;
import com.pastime.util.Location;

@Controller
public class LeaguesController {

    private RestTemplate client;

    private ObjectMapper mapper;
    
    private JdbcTemplate jdbcTemplate;
    
    private String upcomingSql;
    
    private String randomPlayersSql;

    private String leagueSql;
    
    private String qualifyingFranchisesSql;

    private String franchiseSql;

    private String activeFranchisePlayersSql;
    
    private String playerSearchSql;
    
    public LeaguesController(JdbcTemplate jdbcTemplate) {
        initHttpClient();
        this.jdbcTemplate = jdbcTemplate;
        this.upcomingSql = SqlUtils.sql(new ClassPathResource("upcoming-leagues.sql", getClass()));
        this.randomPlayersSql = SqlUtils.sql(new ClassPathResource("random-registered-players.sql", getClass()));
        this.leagueSql = SqlUtils.sql(new ClassPathResource("league.sql", getClass()));        
        this.franchiseSql = SqlUtils.sql(new ClassPathResource("franchise.sql", getClass()));
        this.activeFranchisePlayersSql = SqlUtils.sql(new ClassPathResource("franchise-players-active.sql", getClass()));        
        this.qualifyingFranchisesSql = SqlUtils.sql(new ClassPathResource("qualifying-franchises.sql", getClass()));
        this.playerSearchSql = SqlUtils.sql(new ClassPathResource("player-search.sql", getClass()));        
    }
    
    @RequestMapping(value="/", method=RequestMethod.GET, produces="text/html")
    public String home(Model model) {
        return "home/home";
    }
    
    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> upcomingLeagues() throws URISyntaxException {
        Location location = new Location(28.0674,-80.5595);
        JsonNode json = client.getForObject(new URI("http://localhost:8983/solr/select?wt=json&fl=organization_name,organization_url,organization_logo,league_sport,league_format,league_nature," + 
                "season_name,season_url,season_start_date,season_picture,venue_name&q=*:*&fq=%7B!geofilt%7D&sfield=venue_location&pt=" + location + "&d=25"), JsonNode.class);
        JsonNode docs = json.get("response").get("docs");
        return new ResponseEntity<JsonNode>(docs, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.POST, produces="application/json")
    public ResponseEntity<? extends Object> indexUpcomingLeagues() throws URISyntaxException {
        final ArrayNode docs = mapper.createArrayNode();
        jdbcTemplate.query(upcomingSql, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                ObjectNode doc = mapper.createObjectNode();
                Integer organizationId = rs.getInt("organization_id");
                doc.put("id", "upcoming_seasons:" + rs.getInt("league_id") + ":" + rs.getInt("season_number"));
                doc.put("organization_id", organizationId);                
                doc.put("organization_name", rs.getString("organization_name"));
                String organizationUrl = organizationUrl(organizationId, rs.getString("organization_username"));
                doc.put("organization_url", organizationUrl);
                doc.put("organization_logo", "http://pastime.com/static/images/organizations/1.png");
                doc.put("league_id", rs.getInt("league_id"));
                doc.put("league_sport", rs.getString("league_sport"));
                doc.put("league_format", rs.getString("league_format"));
                doc.put("league_nature", rs.getString("league_nature"));
                doc.put("season_number", rs.getInt("season_number"));
                doc.put("season_name", rs.getString("season_name"));
                doc.put("season_url", organizationUrl + "/" + rs.getString("league_slug") + "/" + rs.getInt("season_number"));                
                doc.put("season_picture", "http://pastime.com/static/images/leagues/1/1.png");
                doc.put("season_start_date", new DateTime(rs.getDate("season_start_date"), DateTimeZone.UTC).toString());
                doc.put("venue_id", rs.getInt("venue_id"));
                doc.put("venue_name", rs.getString("venue_name"));
                doc.put("venue_location", new Location(rs.getDouble("venue_latitude"), rs.getDouble("venue_longitude")).toString());
                docs.add(doc);
            }
            private String organizationUrl(Integer id, String name) {
                // TODO: we could just always send to /organizations/id and rely on redirect if username is set...
                if (name != null) {
                    return "http://pastime.com/" + name;
                } else {
                    return "http://pastime.com/organizations/" + id;
                }
            }            
        });
        client.postForLocation("http://localhost:8983/solr/update/json?commit=true", docs);
        return new ResponseEntity<JsonNode>(HttpStatus.CREATED);
    }
    
    @RequestMapping(value="/leagues/{league}/{season}/random-players", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> randomPlayers(@PathVariable String username, @PathVariable String league, @PathVariable Integer season) {
        final ArrayNode docs = mapper.createArrayNode();
        jdbcTemplate.query(randomPlayersSql, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                ObjectNode doc = mapper.createObjectNode();
                doc.put("id", rs.getInt("id"));
                doc.put("slug", rs.getString("slug"));
                doc.put("number", rs.getInt("number"));
                doc.put("nickname", rs.getString("nickname"));
                docs.add(doc);
            }
        });        
        return new ResponseEntity<JsonNode>(docs, HttpStatus.ACCEPTED);
    }

    // TODO custom request mapping condition required to confirm {organization} is actually a username that identifies an organization
    // See Skype chat history with Rossen
    @RequestMapping(value="/{organization}/{league}/{season}", method=RequestMethod.GET, produces="text/html")
    public String join(@PathVariable String organization, @PathVariable String league, @PathVariable Integer season, Model model) {
        return "leagues/join";
    }
    
    @RequestMapping(value="/{organization}/{league}/{season}", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<? extends Object> league(@PathVariable String organization, @PathVariable String league, @PathVariable Integer season) {
        Map<String, Object> json = jdbcTemplate.queryForMap(leagueSql, organization, league, season);
        json.put("link", "http://pastime.com/leagues/" + json.get("league_id") + "/" + json.get("season_number"));
        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value="/me/franchises", method=RequestMethod.GET, params="league", produces="application/json")
    public ResponseEntity<? extends Object> qualifyingFranchises(@RequestParam Integer league) {
        if (!SecurityContext.signedIn()) {
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
    
    @RequestMapping(value="/leagues/{league}/{season}/teams", method=RequestMethod.POST)
    @Transactional
    public ResponseEntity<? extends Object> addTeam(@PathVariable Integer league, @PathVariable Integer season, @RequestParam Map<String, Object> team) {
        if (!SecurityContext.signedIn()) {
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
        Integer id = jdbcTemplate.queryForInt("SELECT coalesce(max(team) + 1, 1) as next_id FROM teams WHERE league = ? AND season = ?", league, season); 
        jdbcTemplate.update("INSERT INTO teams (league, season, team, name, franchise) VALUES (?, ?, ?, ?, ?)", league, season, id, name, franchise);
        PlayerPrincipal player = SecurityContext.getPrincipal();
        Integer number = null;
        String nickname = null;
        if (franchise != null) {
            Map<String, Object> franchiseInfo = jdbcTemplate.queryForMap("SELECT number, nickname FROM franchise_members WHERE franchise = ? AND player = ?", franchise, player.getId());
            number = (Integer) franchiseInfo.get("number");
            nickname = (String) franchiseInfo.get("nickname");
        }
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", league, season, id, player.getId(), number, nickname);
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)", league, season, id, player.getId(), TeamRoles.ADMIN);                
        return new ResponseEntity<Integer>(id, HttpStatus.ACCEPTED);
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
        
    @RequestMapping(value="/me/players", method=RequestMethod.GET, params="name", produces="application/json")
    // TODO exclude "me" and existing franchise players from search on server or client?
    public ResponseEntity<? extends Object> playerSearch(@RequestParam String name, @RequestParam Integer franchise) {
        if (!SecurityContext.signedIn()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);
        }
        List<Player> players = jdbcTemplate.query(playerSearchSql, new PlayerMapper(null), name + "%");
        return new ResponseEntity<List<Player>>(players, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/{username}/picture", method=RequestMethod.GET)
    public String picture() {
        return "redirect:/static/images/players/18/small.png";        
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