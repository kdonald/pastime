package com.pastime.leagues;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.pastime.util.Location;
import com.pastime.util.Principal;

@RequestMapping
public class UpcomingSeasonsController {

    private String siteUrl = "http://localhost:8080";

    private String solrUrl = "http://localhost:8983/solr";
    
    private RestTemplate client;

    private ObjectMapper mapper;

    private JdbcTemplate jdbcTemplate;

    @Inject
    public UpcomingSeasonsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initHttpClient();
    }

    @RequestMapping(value="/seasons", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> seasons(HttpServletRequest request, Principal principal) throws URISyntaxException {
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
