package com.pastime.prelaunch;

import static org.springframework.dao.support.DataAccessUtils.singleResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pastime.prelaunch.Subscriber.ReferredBy;
import com.pastime.util.DefaultReferralCodeGenerator;
import com.pastime.util.Name;
import com.pastime.util.ReferralCodeGenerator;

public class SubscriptionRepository {

    private ReferralCodeGenerator referralCodeGenerator = new DefaultReferralCodeGenerator();

    private JdbcTemplate jdbcTemplate;

    private SubscriberListener subscriberListener;

    private String applicationUrl = "http://pastime.com";

    public SubscriptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setReferralCodeGenerator(ReferralCodeGenerator referralCodeGenerator) {
        this.referralCodeGenerator = referralCodeGenerator;
    }
    
    public void setSubscriberListener(SubscriberListener subscriberListener) {
        this.subscriberListener = subscriberListener;
    }
    
    public String findFirstName(String referralCode) {
        if (referralCode == null || !referralCodeGenerator.meetsSyntax(referralCode)) {
            return null;
        }
        return singleResult(jdbcTemplate.query("SELECT first_name FROM prelaunch.subscriptions WHERE referral_code = ?", new SingleColumnRowMapper<String>(String.class), referralCode));
    }
    
    @Transactional
    public Subscription subscribe(SubscribeForm form) {
        Subscription subscription = findSubscription(form.getEmail());
        if (subscription != null) {
            return subscription;
        }        
        ReferredBy referredBy = findReferredBy(form.getR());
        String referralCode = generateUniqueReferralCode();
        Date created = new Date();
        String sql = "INSERT INTO prelaunch.subscriptions (email, first_name, last_name, referral_code, referred_by, created) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, form.getEmail(), form.getFirstName(), form.getLastName(), referralCode, referredBy != null ? referredBy.getId() : null, new Date());
        if (subscriberListener != null) {
            subscriberListener.subscriberAdded(new Subscriber(form.getEmail(), new Name(form.getFirstName(), form.getLastName()), referralCode, referredBy, created));
        }
        return new Subscription(form.getFirstName(), referralLink(referralCode));
    }

    public void unsubscribe(String email) {
        jdbcTemplate.update("UPDATE prelaunch.subscriptions SET unsubscribed = true WHERE email = ?", email);        
    }

    // internal helpers
    
    private Subscription findSubscription(String email) {
        String sql = "SELECT id, first_name, referral_code, unsubscribed FROM prelaunch.subscriptions WHERE email = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, email);
        if (!rs.first()) {
            return null;
        }
        boolean unsubscribed = rs.getBoolean("unsubscribed");
        if (unsubscribed) {
            jdbcTemplate.update("UPDATE prelaunch.subscriptions SET unsubscribed = false WHERE id  = ?", rs.getInt("id"));
        }
        return new Subscription(rs.getString("first_name"), referralLink(rs.getString("referral_code")));
    }

    private ReferredBy findReferredBy(String r) {
        if (!StringUtils.hasText(r)) {
            return null;
        }
        String sql = "SELECT id, first_name, last_name, email, referral_code FROM prelaunch.subscriptions WHERE referral_code = ?";
        return singleResult(jdbcTemplate.query(sql,
                new RowMapper<ReferredBy>() {
                    public ReferredBy mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new ReferredBy(rs.getInt("id"), new Name(rs.getString("first_name"), rs.getString("last_name")), rs.getString("email"), rs.getString("referral_code"));
                    }
                }, r));
    }

    private String generateUniqueReferralCode() {
        do {
            String referralCode = referralCodeGenerator.generateKey();
            if (!jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM prelaunch.subscriptions WHERE referral_code = ?)", Boolean.class, referralCode)) {
                return referralCode;
            }
        } while (true);
    }
    
    private String referralLink(String referralCode) {
        return applicationUrl + "/?r=" + referralCode;
    }
    
    // cglib ceremony 
    public SubscriptionRepository() {}
    
}