package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.templating.StringTemplateLoader;

import com.pastime.prelaunch.PrelaunchController;
import com.pastime.prelaunch.referrals.ReferralProgram;
import com.pastime.prelaunch.referrals.ReferralsController;

@Configuration
public class PrelaunchConfig {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private RedisOperations<String, String> redisOperations;

    @Inject
    private JavaMailSender mailSender;

    @Inject
    private StringTemplateLoader templateLoader;
    
    @Bean
    public PrelaunchController prelaunchController() {
        PrelaunchController controller = new PrelaunchController(jdbcTemplate);
        controller.setSubscriberListener(referralProgram());
        return controller;
    }
    
    @Bean
    public ReferralsController referralsController() {
        return new ReferralsController(referralProgram());
    }
    
    @Bean
    public ReferralProgram referralProgram()  {
        return new ReferralProgram(redisOperations, mailSender, templateLoader);
    }
    
}