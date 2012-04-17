package com.pastime.players;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pastime.TestDataAccessConfig;
import com.pastime.players.images.ImagesReference;
import com.pastime.util.PastimeEnvironment;

@Configuration
@Import(TestDataAccessConfig.class)
public class PlayerRepositoryTestsConfig {

    @Bean
    public PlayerRepository playerRepository(JdbcTemplate jdbcTemplate, PastimeEnvironment environment) {
        ResourceLoader resourceLoader = new ClassRelativeResourceLoader(ImagesReference.class);
        return new PlayerRepository(jdbcTemplate, environment, resourceLoader);
    }
        
}