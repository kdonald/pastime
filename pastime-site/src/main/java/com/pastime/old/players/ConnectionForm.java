package com.pastime.old.players;

import org.hibernate.validator.constraints.NotEmpty;

public class ConnectionForm {
    
    @NotEmpty
    private String provider;
    
    @NotEmpty
    private String user_id;
    
    @NotEmpty
    private String access_token;
    
    private String scope;
    
    private Long expires_in;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        System.out.println(provider);
        this.provider = provider.trim();
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        System.out.println(user_id);        
        this.user_id = user_id;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        System.out.println(access_token);        
        this.access_token = access_token;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        System.out.println(scope);        
        this.scope = scope.trim();
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        System.out.println(expires_in);        
        this.expires_in = expires_in;
    }
    
}
