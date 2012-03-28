package com.pastime.prelaunch;

public class Subscription {
    
    private final String firstName;
    
    private final String referralLink;

    public Subscription(String firstName, String referralLink) {
        this.firstName = firstName;
        this.referralLink = referralLink;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getReferralLink() {
        return referralLink;
    }
    
}