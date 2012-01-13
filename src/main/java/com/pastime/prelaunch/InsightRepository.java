package com.pastime.prelaunch;

import javax.inject.Inject;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

@Repository
public class InsightRepository {
    
    private final RedisOperations<String, String> redisOperations;
    
    @Inject
    public InsightRepository(RedisOperations<String, String> redisOperations) {
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

    public ReferralInsights getReferralInsights(String referralCode) {
        BoundValueOperations<String, String> valueOps = redisOperations.boundValueOps(referralCode);
        String value = valueOps.get();
        if (value == null) {
            return null;
        }
        return new ReferralInsights(value);
    }
    
    public final class ReferralInsights {
        
        private final int totalReferralCount;
        
        public ReferralInsights(String totalReferralCount) {
            this.totalReferralCount = Integer.parseInt(totalReferralCount);
        }

        public int getTotalReferralCount() {
            return totalReferralCount;
        }

    }

}