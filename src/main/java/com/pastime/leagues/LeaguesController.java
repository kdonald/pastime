package com.pastime.leagues;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.pastime.util.Location;

@Controller
public class LeaguesController {

    private RestTemplate client;

    private ObjectMapper mapper;
    
    private JdbcTemplate jdbcTemplate;
    
    private String upcomingSql;
    
    private String playersSql;
    
    public LeaguesController(JdbcTemplate jdbcTemplate) {
        initHttpClient();
        this.jdbcTemplate = jdbcTemplate;
        this.upcomingSql = SqlUtils.sql(new ClassPathResource("upcoming-leagues.sql", getClass()));
        this.playersSql = SqlUtils.sql(new ClassPathResource("players.sql", getClass()));
    }
    
    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> upcoming() throws URISyntaxException {
        Location location = new Location(28.0674,-80.5595);
        JsonNode json = client.getForObject(new URI("http://localhost:8983/solr/select?wt=json&fl=organization_name,organization_url,organization_logo,league_sport,league_format,league_nature," + 
                "season_name,season_url,season_picture,venue_name&q=*:*&fq=%7B!geofilt%7D&sfield=venue_location&pt=" + location + "&d=25"), JsonNode.class);
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
    
    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.POST, produces="application/json")
    public ResponseEntity<? extends Object> postUpcoming() throws URISyntaxException {
        final ArrayNode docs = mapper.createArrayNode();
        jdbcTemplate.query(upcomingSql, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                ObjectNode doc = mapper.createObjectNode();
                Integer organizationId = rs.getInt("organization_id");
                doc.put("id", "upcomingSeasons:" + rs.getInt("league_id") + ":" + rs.getInt("season_number"));
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
                    return "http://localhost:8080/" + name;
                } else {
                    return "http://localhost:8080/organizations/" + id;
                }
            }            
        });
        client.postForLocation("http://localhost:8983/solr/update/json?commit=true", docs);
        return new ResponseEntity<JsonNode>(HttpStatus.CREATED);
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