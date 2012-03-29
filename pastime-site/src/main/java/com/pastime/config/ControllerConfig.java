package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pastime.SiteController;

@Configuration
public class ControllerConfig {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Bean
    public SiteController siteController() {
        return new SiteController(jdbcTemplate);
    }
    
}