package com.pastime.leagues;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.pastime.util.Location;

@Controller
public class LeaguesController {

    private ObjectMapper mapper;
    
    private RestTemplate client;
    
    public LeaguesController() {
        initHttpClient();
    }
    
    @RequestMapping(value="/leagues/upcoming", method=RequestMethod.GET, produces="application/json")
    public ResponseEntity<JsonNode> upcoming() throws URISyntaxException {
        Location location = new Location(28.0674,-80.5595);
        JsonNode json = client.getForObject(new URI("http://localhost:8983/solr/select?wt=json&fl=*&q=*:*&fq=%7B!geofilt%7D&sfield=venue_location&pt=" + location + "&d=25"), JsonNode.class);
        JsonNode docs = json.get("response").get("docs");
        ObjectNode upcoming = mapper.createObjectNode();
        upcoming.put("upcomingSeasons", docs);
        return new ResponseEntity<JsonNode>(upcoming, HttpStatus.ACCEPTED);
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