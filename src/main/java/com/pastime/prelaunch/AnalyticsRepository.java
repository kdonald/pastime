package com.pastime.prelaunch;

import javax.inject.Inject;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

@Repository
public class AnalyticsRepository {
    
    private final RedisOperations<String, String> redisOperations;
    
    @Inject
    public AnalyticsRepository(RedisOperations<String, String> redisOperations) {
        this.redisOperations = redisOperations;
    }

    /**
     * On a new referred subscriber, update analytics.
     * @param referralCode the referral code.
     */
    public void subscriberAdded(Subscriber subscriber) {
        redisOperations.boundValueOps(subscriber.getReferralCode()).set("0");
        if (subscriber.getReferredBy() != null) {
            redisOperations.boundValueOps(subscriber.getReferredBy()).increment(1);        
        }
    }

    public ReferralAnalytics getReferralAnalytics(String referralCode) {
        BoundValueOperations<String, String> valueOps = redisOperations.boundValueOps(referralCode);
        return new ReferralAnalytics(valueOps.get());
    }
    
    public final class ReferralAnalytics {
        
        private final int totalReferralCount;
        
        public ReferralAnalytics(String totalReferralCount) {
            this.totalReferralCount = Integer.parseInt(totalReferralCount);
        }

        public int getTotalReferralCount() {
            return totalReferralCount;
        }

    }

}