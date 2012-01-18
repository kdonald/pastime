package com.pastime.prelaunch.referrals;

public class Referral {
    
    private final String date;
    
    private final String name;
    
    private final String referredBy;

    public Referral(String date, String name, String referredBy) {
        this.date = date;
        this.name = name;
        this.referredBy = referredBy;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getReferredBy() {
        return referredBy;
    }    
    
}
