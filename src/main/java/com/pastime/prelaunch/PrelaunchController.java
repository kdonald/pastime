package com.pastime.prelaunch;

import static org.springframework.dao.support.DataAccessUtils.singleResult;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PrelaunchController {

    private final StringKeyGenerator referralCodeGenerator = new ReferralCodeGenerator();
    
    private final JdbcTemplate jdbcTemplate;
    
    private final SubscriberListener subscriberListener;
    
    @Inject
    public PrelaunchController(JdbcTemplate jdbcTemplate, SubscriberListener subscriberListener) {
        this.jdbcTemplate = jdbcTemplate;
        this.subscriberListener = subscriberListener;
    }

    @RequestMapping(value="/", method=RequestMethod.GET)
    public String index(Model model) {
        return "prelaunch/index";
    }

    @RequestMapping(value="/", method=RequestMethod.POST)
    public @ResponseBody Subscription subscribe(@Valid SubscribeForm form) {
        Subscription subscription = findSubscription(form.getEmail());
        if (subscription != null) {
            return subscription;
        }
        return createSubscription(form.getEmail(), new Name(form.getFirstName(), form.getLastName()), form.getR());
    }
    
    @RequestMapping(value="/privacy", method=RequestMethod.GET)
    public String privacy(Model model) {
        return "prelaunch/privacy";
    }

    @RequestMapping(value="/unsubscribe", method=RequestMethod.GET)
    public String unsubscribeForm(Model model) {
        return "prelaunch/unsubscribe";
    }

    @RequestMapping(value="/unsubscribe", method=RequestMethod.POST)
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
        Integer referredBy = singleResult(jdbcTemplate.query("SELECT id FROM prelaunch.subscriptions WHERE referral_code = ?", new SingleColumnRowMapper<Integer>(Integer.class), r));
        String referralCode = referralCodeGenerator.generateKey();
        String sql = "INSERT INTO prelaunch.subscriptions (email, first_name, last_name, referred_by, referral_code) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, email, name.getFirstName(), name.getLastName(), referredBy, referralCode);
        subscriberListener.subscriberAdded(new Subscriber(email, name, referralCode, r));
        return new Subscription(name.getFirstName(), referralLink(referralCode));
    }

    private String referralLink(String referralCode) {
        return "http://pastimebrevard.com?r=" +  referralCode;
    }
    
}