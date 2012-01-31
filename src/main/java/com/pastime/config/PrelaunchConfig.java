package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pastime.prelaunch.PrelaunchController;
import com.pastime.prelaunch.SubscriberListeners;
import com.pastime.prelaunch.SubscriptionRepository;
import com.pastime.prelaunch.WelcomeMailer;
import com.pastime.prelaunch.admin.AdminInterceptor;
import com.pastime.prelaunch.admin.ReferralsAdminController;
import com.pastime.prelaunch.referrals.ReferralProgram;
import com.pastime.prelaunch.referrals.ReferralsController;

@Configuration
public class PrelaunchConfig extends WebMvcConfigurerAdapter {

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
        return new PrelaunchController(subscriptionRepository());
    }

    @Bean
    public SubscriptionRepository subscriptionRepository() {
        SubscriptionRepository repository = new SubscriptionRepository(jdbcTemplate);
        SubscriberListeners listeners = new SubscriberListeners();
        listeners.add(welcomeMailer());
        listeners.add(referralProgram());
        repository.setSubscriberListener(listeners);
        return repository;
    }
    
    @Bean
    public WelcomeMailer welcomeMailer() {
        return new WelcomeMailer(mailSender, templateLoader);
    }

    // referrals sub-module
    
    @Bean
    public ReferralsController referralsController() {
        return new ReferralsController(referralProgram(), subscriptionRepository());
    }

    @Bean
    public ReferralProgram referralProgram()  {
        return new ReferralProgram(redisOperations);
    }

    // admin sub-module
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminInterceptor()).addPathPatterns("/admin/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin").setViewName("prelaunch/admin/authenticate");
    }    

    @Bean
    public ReferralsAdminController referralsAdminController() {
        return new ReferralsAdminController(referralProgram());
    }
    
}