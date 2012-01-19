package com.pastime.prelaunch;

public class Subscriber {
    
    private final String email;
    
    private final Name name;
    
    private final String referralCode;

    private final ReferredBy referredBy;
    
    public Subscriber(String email, Name name, String referralCode, ReferredBy referredBy) {
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

    public ReferredBy getReferredBy() {
        return referredBy;
    }
    
    public static final class ReferredBy {

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
    
}