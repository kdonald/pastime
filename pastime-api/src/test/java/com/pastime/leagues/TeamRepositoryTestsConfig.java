package com.pastime.leagues;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pastime.TestDataAccessConfig;
import com.pastime.leagues.season.TeamRepository;
import com.pastime.util.PastimeEnvironment;

@Import(value={ TestDataAccessConfig.class })
public class TeamRepositoryTestsConfig {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private PastimeEnvironment environment;

    @Bean
    public TeamRepository teamRepository() {
        return new TeamRepository(jdbcTemplate, environment);
    }
    
}