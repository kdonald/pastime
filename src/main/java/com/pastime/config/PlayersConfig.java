package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pastime.players.SecurityController;

@Configuration
public class PlayersConfig extends WebMvcConfigurerAdapter {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Bean
    public SecurityController signupController() {
        return new SecurityController(jdbcTemplate);
    }
    
}