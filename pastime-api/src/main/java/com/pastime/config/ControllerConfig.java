package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.pastime.errors.ErrorController;
import com.pastime.franchises.FranchiseController;
import com.pastime.franchises.FranchiseRepository;
import com.pastime.franchises.MyFranchisesController;
import com.pastime.leagues.UpcomingSeasonsController;
import com.pastime.leagues.season.SeasonController;
import com.pastime.leagues.season.TeamRepository;
import com.pastime.players.PlayerRepository;
import com.pastime.players.PlayersController;
import com.pastime.util.PastimeEnvironment;

@Configuration
public class ControllerConfig extends WebMvcConfigurerAdapter {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JavaMailSender mailSender;

    @Inject
    private StringTemplateLoader templateLoader;
    
    @Inject
    private ResourceLoader resourceLoader;
    
    @Bean
    public PastimeEnvironment environment() {
        return new PastimeEnvironment("http://localhost:8081", "http://localhost:8080");
    }

    @Bean
    public ErrorController errorController() {
        return new ErrorController();
    }
    
    @Bean
    public UpcomingSeasonsController upcomingSeasonsController() {
        return new UpcomingSeasonsController(jdbcTemplate);
    }
    
    @Bean
    public PlayersController playerController() {
        PlayerRepository playerRepository = new PlayerRepository(jdbcTemplate, environment(), resourceLoader);
        return new PlayersController(playerRepository);
    }

    @Bean
    public FranchiseRepository franchiseRepository() {
        return new FranchiseRepository(jdbcTemplate, environment());
    }
    
    @Bean
    public FranchiseController franchiseController() {
        return new FranchiseController(franchiseRepository());
    }
    
    @Bean
    public MyFranchisesController myFranchisesController() {
        return new MyFranchisesController(franchiseRepository());
    }
    
    @Bean
    public SeasonController seasonController() {
        TeamRepository teamRepository = new TeamRepository(jdbcTemplate, mailSender, templateLoader, environment());
        return new SeasonController(teamRepository);
    }
    
}