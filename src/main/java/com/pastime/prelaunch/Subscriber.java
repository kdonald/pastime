package com.pastime.prelaunch;

public class Subscriber {
    
    private final String email;
    
    private final Name name;
    
    private final String referredBy;
    
    private final String referralCode;

    public Subscriber(String email, Name name, String referralCode, String referredBy) {
        this.email = email;
        this.name = name;
        this.referralCode = referralCode;
        this.referredBy = referredBy;        
    }

    public String getEmail() {
        return email;
    }

    public Name getName() {
        return name;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public String getReferredBy() {
        return referredBy;
    }
    
}
