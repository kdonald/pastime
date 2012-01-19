package com.pastime.prelaunch.referrals;

public class Referral {
    
    private final String date;
    
    private final String name;
    
    private final String referredBy;

    private final String referralCode;
    
    public Referral(String date, String name, String referredBy, String referralCode) {
        this.date = date;
        this.name = name;
        this.referredBy = referredBy;
        this.referralCode = referralCode;
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

    public String getReferralCode() {
        return referralCode;
    }
    
}
