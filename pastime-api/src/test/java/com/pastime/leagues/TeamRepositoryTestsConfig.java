package com.pastime.leagues;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.TestDataAccessConfig;
import com.pastime.leagues.season.TeamRepository;
import com.pastime.players.PlayerRepository;

@Import(value=TestDataAccessConfig.class)
public class TeamRepositoryTestsConfig {

    @Bean
    public TeamRepository teamRepository(JdbcTemplate jdbcTemplate) {
        return new TeamRepository(jdbcTemplate);
    }
    
}