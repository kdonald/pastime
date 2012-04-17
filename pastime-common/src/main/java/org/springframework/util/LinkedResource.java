package org.springframework.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LinkedResource {

	private final URI url;
	
    private final Map<String, URI> links = new HashMap<String, URI>();
    
    public LinkedResource(URI url) {
		this.url = url;
	}

	public URI getUrl() {
    	return url;
    }
    
    public Map<String, URI> getLinks() {
        return links;
    }
    
    public void addLink(String name, URI value) {
        links.put(name, value);
    }
   
}
