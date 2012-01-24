package com.pastime.prelaunch;

import static org.springframework.dao.support.DataAccessUtils.singleResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.validation.Valid;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.prelaunch.Subscriber.ReferredBy;

@Controller
public class PrelaunchController {

    private StringKeyGenerator referralCodeGenerator = new ReferralCodeGenerator();

    private JdbcTemplate jdbcTemplate;

    private SubscriberListener subscriberListener;
    
    private String applicationUrl = "http://pastimebrevard.com";
    
    public PrelaunchController(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("jdbcTemplate cannot be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setReferralCodeGenerator(StringKeyGenerator referralCodeGenerator) {
        if (referralCodeGenerator == null) {
            throw new IllegalArgumentException("cannot be null");
        }
        this.referralCodeGenerator = referralCodeGenerator;
    }
    
    public void setSubscriberListener(SubscriberListener subscriberListener) {
        this.subscriberListener = subscriberListener;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        return "prelaunch/index";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Subscription subscribe(@Valid SubscribeForm form) {
        Subscription subscription = findSubscription(form.getEmail());
        if (subscription != null) {
            return subscription;
        }
        return createSubscription(form.getEmail(), new Name(form.getFirstName(), form.getLastName()), form.getR());
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String privacy(Model model) {
        return "prelaunch/about";
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.GET)
    public String unsubscribeForm(Model model) {
        return "prelaunch/unsubscribe";
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public String unsubscribe(@Valid UnsubscribeForm form) {
        jdbcTemplate.update("UPDATE prelaunch.subscriptions SET unsubscribed = true WHERE email = ?", form.getEmail());
        return "redirect:/";
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

    private Subscription createSubscription(String email, Name name, String r) {
        ReferredBy referredBy = findReferredBy(r);
        String referralCode = generateUniqueReferralCode();
        Date created = new Date();
        String sql = "INSERT INTO prelaunch.subscriptions (email, first_name, last_name, referral_code, referred_by, created) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, email, name.getFirstName(), name.getLastName(), referralCode, referredBy != null ? referredBy.getId() : null, new Date());
        if (subscriberListener != null) {
            subscriberListener.subscriberAdded(new Subscriber(email, name, referralCode, referredBy, created));
        }
        return new Subscription(name.getFirstName(), referralLink(referralCode));
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
        return applicationUrl + "?r=" + referralCode;
    }

    // cglib ceremony
    public PrelaunchController() {}
    
}