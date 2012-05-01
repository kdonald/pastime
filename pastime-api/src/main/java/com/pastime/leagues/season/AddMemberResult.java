package com.pastime.leagues.season;

import java.net.URI;

public final class AddMemberResult {
    
    private final ResultType type;
    
    private final URI link;

    public ResultType getType() {
        return type;
    }

    public URI getLink() {
        return link;
    }

    public static enum ResultType {
        MEMBER_CONFIRMED, MEMBER_INVITED
    }

    public static AddMemberResult confirmed(URI link) {
        return new AddMemberResult(ResultType.MEMBER_CONFIRMED, link);
    }

    public static AddMemberResult invited(URI link) {
        return new AddMemberResult(ResultType.MEMBER_INVITED, link);
    }
    
    private AddMemberResult(ResultType type, URI link) {
        this.type = type;
        this.link = link;
    }

}