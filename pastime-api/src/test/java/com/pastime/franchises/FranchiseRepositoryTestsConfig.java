package com.pastime.franchises;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pastime.TestDataAccessConfig;
import com.pastime.util.PastimeEnvironment;

@Configuration
@Import(TestDataAccessConfig.class)
public class FranchiseRepositoryTestsConfig {

    @Bean
    public FranchiseRepository playerRepository(JdbcTemplate jdbcTemplate, PastimeEnvironment environment) {
        return new FranchiseRepository(jdbcTemplate, environment);
    }
        
}