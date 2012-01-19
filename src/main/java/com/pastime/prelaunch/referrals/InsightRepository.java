package com.pastime.prelaunch.referrals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.pastime.prelaunch.Subscriber;
import com.pastime.prelaunch.Subscriber.ReferredBy;
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
            captureReferralInsights(subscriber);        
        }
    }

    public Integer getTotalReferrals() {
        String total = redisOperations.opsForValue().get(TOTAL_REFERRALS);
        return total != null ? Integer.parseInt(total) : 0;        
    }

    public Integer getTotalReferrals(String referralCode) {
        String total = redisOperations.opsForValue().get(totalReferralsForCode(referralCode));
        return total != null ? Integer.parseInt(total) : null;
    }

    public List<Referral> getReferrals() {
        Set<TypedTuple<String>> results = redisOperations.opsForZSet().reverseRangeWithScores(REFERRALS_BY_DATE, 0, Integer.MAX_VALUE);
        if (results.size() == 0) {
            return Collections.emptyList();
        }
        List<Referral> referrals = new ArrayList<Referral>(results.size());
        SimpleDateFormat format = new SimpleDateFormat("MMMM d");
        for (TypedTuple<String> result : results) {
            Date date = new Date(result.getScore().longValue());
            Map<Object, Object> row = redisOperations.opsForHash().entries(referral(result.getValue()));
            referrals.add(new Referral(format.format(date), (String) row.get("name"), (String) row.get("referredBy")));
        }
        return referrals;
    }
    
    // internal helpers
    
    private void initReferralCounts(String referralCode) {
        redisOperations.boundValueOps(totalReferralsForCode(referralCode)).set("0");        
    }
    
    private void captureReferralInsights(Subscriber subscriber) {
        ReferredBy referredBy = subscriber.getReferredBy();
        Long referralId = redisOperations.opsForValue().increment(TOTAL_REFERRALS, 1);
        redisOperations.opsForValue().increment(totalReferralsForCode(referredBy.getReferralCode()), 1);

        Map<String, String> m  = new HashMap<String, String>();
        m.put("name", subscriber.getName().getPublicDisplayName());
        m.put("referredBy", referredBy.getName().getPublicDisplayName());
        redisOperations.opsForHash().putAll(referral(referralId), m);
        
        redisOperations.boundZSetOps(REFERRALS_BY_DATE).add(referralId.toString(), new Date().getTime());
    }

    private static String totalReferralsForCode(String referralCode) {
        return TOTAL_REFERRALS_FOR_CODE + referralCode;
    }

    private String referral(Object referralId) {
        return REFERRALS + ":" + referralId;
    }
    
    private static final String REFERRALS = "referrals";
    
    private static final String REFERRALS_BY_DATE = REFERRALS + "_by_date";
    
    // private static final String REFERRALS_BY_DATE_FOR_CODE = REFERRALS_BY_DATE + "_for_code:";
    
    private static final String TOTAL_REFERRALS = REFERRALS + "_counts_total";
    
    private static final String TOTAL_REFERRALS_FOR_CODE = TOTAL_REFERRALS + "_for_code:";

}