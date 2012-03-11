package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pastime.leagues.LeaguesController;
import com.pastime.players.AccountController;
import com.pastime.players.SigninInterceptor;
import com.pastime.players.TeamsController;

@Configuration
public class PlayersConfig extends WebMvcConfigurerAdapter {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JavaMailSender mailSender;

    @Inject
    private StringTemplateLoader templateLoader;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SigninInterceptor());
    }

    @Bean
    public AccountController accountController() {
        return new AccountController(jdbcTemplate);
    }
    
    @Bean
    public TeamsController teamsController() {
        return new TeamsController(jdbcTemplate, mailSender, templateLoader);
    }
    
    @Bean
    public LeaguesController leaguesController() {
        return new LeaguesController();
    }
    
}