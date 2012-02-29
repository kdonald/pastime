package com.pastime.players;


public class ReferredBy {

    private final Integer id;
    
    private final Name name;

    private final String email;
    
    private final String referralCode;

    public ReferredBy(Integer id, Name name, String email, String referralCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.referralCode = referralCode;
    }

    public Integer getId() {
        return id;
    }
    
    public Name getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getReferralCode() {
        return referralCode;
    }
    
}