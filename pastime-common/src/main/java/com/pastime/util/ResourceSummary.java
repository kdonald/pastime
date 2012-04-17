package com.pastime.util;

import java.net.URI;

public class ResourceSummary {

    private final String name;
    
    private final URI link;

    public ResourceSummary(String name, URI link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public URI getLink() {
        return link;
    }

}
