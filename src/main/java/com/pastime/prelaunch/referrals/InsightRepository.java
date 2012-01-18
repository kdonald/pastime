package com.pastime.prelaunch.referrals;

import javax.inject.Inject;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import com.pastime.prelaunch.Subscriber;
import com.pastime.prelaunch.SubscriberListener;


@Repository
public class InsightRepository implements SubscriberListener {
    
    private final RedisOperations<String, String> redisOperations;
    
    @Inject
    public InsightRepository(RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    @Override
    public void subscriberAdded(Subscriber subscriber) {
        initReferralCounts(subscriber.getReferralCode());
        if (subscriber.getReferredBy() != null) {
            incrementReferralCounts(subscriber.getReferredBy());        
        }
    }

     public ReferralInsights getReferralInsights() {
        String total = redisOperations.opsForValue().get(REFERRALS);
        if (total == null) {
            return ReferralInsights.ZERO;
        }
        return new ReferralInsights(total);
    }

    public ReferralInsights getReferralInsights(String referralCode) {
        BoundValueOperations<String, String> valueOps = redisOperations.boundValueOps(referralCode(referralCode));
        String total = valueOps.get();
        if (total == null) {
            return null;
        }
        return new ReferralInsights(total);
    }

    private void initReferralCounts(String referralCode) {
        redisOperations.boundValueOps(referralCode(referralCode)).set("0");        
    }
    
    private void incrementReferralCounts(String referralCode) {
        redisOperations.opsForValue().increment(REFERRALS, 1);
        redisOperations.boundValueOps(referralCode(referralCode)).increment(1);
    }

    public static final class ReferralInsights {
        
        public static final ReferralInsights ZERO = new ReferralInsights();
        
        private final int total;
        
        public ReferralInsights(String total) {
            this.total = Integer.parseInt(total);
        }

        public int getTotal() {
            return total;
        }
        
        private ReferralInsights() { this.total = 0; }
    }

    private static String referralCode(String referralCode) {
        return REFERRAL_CODE + referralCode;
    }
    
    private static final String REFERRALS = "referrals";
    
    private static final String REFERRAL_CODE = REFERRALS + "_code_";

}