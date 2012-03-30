package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pastime.leagues.LeaguesController;

@Configuration
public class ControllerConfig extends WebMvcConfigurerAdapter {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JavaMailSender mailSender;

    @Inject
    private StringTemplateLoader templateLoader;
    
    @Bean
    public LeaguesController leaguesController() {
        return new LeaguesController(jdbcTemplate, mailSender, templateLoader);
    }
    
}