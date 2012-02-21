package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pastime.next.NextController;

@Configuration
public class NextConfig {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Bean
    public NextController nextController() {
        return new NextController(jdbcTemplate);
    }

}
