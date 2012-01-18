package com.pastime.prelaunch;

import static org.springframework.dao.support.DataAccessUtils.singleResult;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.validation.Valid;

import org.hibernate.validator.constraints.Email;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class PrelaunchController {

    private final StringKeyGenerator referralCodeGenerator = new ReferralCodeGenerator();
    
    private final JdbcTemplate jdbcTemplate;
    
    private final InsightRepository inisghtRepository;
    
    @Inject
    public PrelaunchController(JdbcTemplate jdbcTemplate, InsightRepository insightRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.inisghtRepository = insightRepository;
    }

    @RequestMapping(value="/", method=RequestMethod.GET)
    public String comingSoon(Model model) {
        return "comingsoon";
    }

    @RequestMapping(value="/", method=RequestMethod.POST)
    public @ResponseBody Subscription subscribe(@Valid SubscribeForm form) {
        Subscription subscription = findSubscription(form.getEmail());
        if (subscription != null) {
            return subscription;
        }
        return createSubscription(form.getEmail(), new Name(form.getFirstName(), form.getLastName()), form.getRef());
    }
    
    @RequestMapping(value="/privacy", method=RequestMethod.GET)
    public String privacy(Model model) {
        return "privacy";
    }

    @RequestMapping(value="/unsubscribe", method=RequestMethod.GET)
    public String unsubscribeForm(Model model) {
        return "unsubscribe";
    }

    @RequestMapping(value="/unsubscribe", method=RequestMethod.POST)
    public String unsubscribe(@Valid UnsubscribeForm form) {
        jdbcTemplate.update("UPDATE prelaunch.subscriptions SET unsubscribed = true WHERE email = ?", form.getEmail());
        return "redirect:/";
    }
    
    // internal helpers
    
    private Subscription findSubscription(String email) {
        String sql = "SELECT first_name, referral_code FROM prelaunch.subscriptions WHERE email = ?";
        return singleResult(jdbcTemplate.query(sql, new RowMapper<Subscription>() {
            public Subscription mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Subscription(rs.getString("first_name"), referralLink(rs.getString("referral_code")));
            }
        }, email));
    }    

    private Subscription createSubscription(String email, Name name, String ref) {
        Integer referredBy = singleResult(jdbcTemplate.query("SELECT id FROM prelaunch.subscriptions WHERE referral_code = ?", new SingleColumnRowMapper<Integer>(Integer.class), ref));
        String referralCode = referralCodeGenerator.generateKey();
        String sql = "INSERT INTO prelaunch.subscriptions (email, first_name, last_name, referred_by, referral_code) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, email, name.getFirstName(), name.getLastName(), referredBy, referralCode);
        inisghtRepository.subscriberAdded(new Subscriber(email, name, referralCode, ref));
        return new Subscription(name.getFirstName(), referralLink(referralCode));
    }

    private String referralLink(String referralCode) {
        return "http://pastimebrevard.com?ref=" +  referralCode;
    }
    
}