package com.pastime.leagues;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.pastime.players.ErrorBody;
import com.pastime.players.SecurityContext;
import com.pastime.util.Location;

@Controller
public class LeaguesController {

    private RestTemplate client;

    private ObjectMapper mapper;
    
    private JdbcTemplate jdbcTemplate;
    
    private String upcomingSql;
    
    private String playersSql;
    
    private String qualifyingFranchisesSql;
    
    private String leagueSql;
    
    public LeaguesController(JdbcTemplate jdbcTemplate) {
        initHttpClient();
        this.jdbcTemplate = jdbcTemplate;
        this.upcomingSql = SqlUtils.sql(new ClassPathResource("upcoming-leagues.sql", getClass()));
        this.playersSql = SqlUtils.sql(new ClassPathResource("players.sql", getClass()));
        this.qualifyingFranchisesSql = SqlUtils.sql(new ClassPathResource("qualifying-franchises.sql", getClass()));
        this.leagueSql = SqlUtils.sql(new ClassPathResource("league.sql", getClass()));
    }
    
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String home(Model model) {
        return "home/home";
    }
    
    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> upcoming() throws URISyntaxException {
        Location location = new Location(28.0674,-80.5595);
        JsonNode json = client.getForObject(new URI("http://localhost:8983/solr/select?wt=json&fl=organization_name,organization_url,organization_logo,league_sport,league_format,league_nature," + 
                "season_name,season_url,season_start_date,season_picture,venue_name&q=*:*&fq=%7B!geofilt%7D&sfield=venue_location&pt=" + location + "&d=25"), JsonNode.class);
        JsonNode docs = json.get("response").get("docs");
        return new ResponseEntity<JsonNode>(docs, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/{username}/{league}/{season}/players", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> players(@PathVariable String username, @PathVariable String league, @PathVariable Integer season) {
        final ArrayNode docs = mapper.createArrayNode();
        jdbcTemplate.query(playersSql, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                ObjectNode doc = mapper.createObjectNode();
                doc.put("id", rs.getInt("id"));
                doc.put("slug", rs.getString("slug"));
                doc.put("picture", rs.getString("picture"));
                doc.put("number", rs.getInt("number"));
                doc.put("nickname", rs.getString("nickname"));
                docs.add(doc);
            }
        });        
        return new ResponseEntity<JsonNode>(docs, HttpStatus.ACCEPTED);
    }

    // TODO custom request mapping condition required to confirm {organization} is actually a username that identifies an organization
    @RequestMapping(value="/{organization}/{league}/{season}", method=RequestMethod.GET)
    public String join(@PathVariable String organization, @PathVariable String league, @PathVariable Integer season, Model model) {
        return "leagues/join";
    }
    
    @RequestMapping(value="/{organization}/{league}/{season}", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<? extends Object> league(@PathVariable String organization, @PathVariable String league, @PathVariable Integer season) {
        Map<String, Object> json = jdbcTemplate.queryForMap(leagueSql, organization, league, season);
        return new ResponseEntity<Map<String, Object>>(json, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.POST, produces="application/json")
    public ResponseEntity<? extends Object> postUpcoming() throws URISyntaxException {
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
                doc.put("organization_logo", rs.getString("organization_logo"));
                doc.put("league_id", rs.getInt("league_id"));
                doc.put("league_sport", rs.getString("league_sport"));
                doc.put("league_format", rs.getString("league_format"));
                doc.put("league_nature", rs.getString("league_nature"));
                doc.put("season_number", rs.getInt("season_number"));
                doc.put("season_name", rs.getString("season_name"));
                doc.put("season_url", organizationUrl + "/" + rs.getString("league_slug") + "/" + rs.getInt("season_number"));                
                doc.put("season_picture", rs.getString("season_picture"));
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
    
    @RequestMapping(value="/me/franchises", method=RequestMethod.GET, params="league", produces="application/json")
    public ResponseEntity<? extends Object> qualifyingFranchises(@RequestParam Integer league) {
        if (!SecurityContext.playerSignedIn()) {
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
        }, SecurityContext.getCurrentPlayer().getId(), league);
        return new ResponseEntity<List<JsonNode>>(franchises, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/me/players", method=RequestMethod.GET, params="name", produces="application/json")
    public ResponseEntity<? extends Object> players(@RequestParam String name, @RequestParam Integer franchise) {
        if (!SecurityContext.playerSignedIn()) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("not authorized"), HttpStatus.FORBIDDEN);
        }
        List<Map<String, Object>> players = new ArrayList<Map<String, Object>>();
        Map<String, Object> first = new HashMap<String, Object>();
        first.put("id", 18);
        first.put("first_name", "Alexander");
        first.put("last_name", "Weaver");
        first.put("number", "37");
        first.put("nickname", "Dream Weaver");
        first.put("link", "http://pastime.com/dream-weaver");
        players.add(first);
        return new ResponseEntity<List<Map<String, Object>>>(players, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(value="/{username}/picture", method=RequestMethod.GET)
    public String picture() {
        return "redirect:/static/images/players/18/small.png";        
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
    
}