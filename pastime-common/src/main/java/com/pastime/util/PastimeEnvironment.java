package com.pastime.util;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

public class PastimeEnvironment {
    
    private URI apiUrl;
    
    private URI siteUrl;

    public PastimeEnvironment(String apiUrl, String siteUrl) {
        this.apiUrl = UriComponentsBuilder.fromUriString(apiUrl).build().toUri();
        this.siteUrl = UriComponentsBuilder.fromUriString(siteUrl).build().toUri();
    }

    public URI getApiUrl() {
        return apiUrl;
    }

    public URI getSiteUrl() {
        return siteUrl;
    }
    
}
