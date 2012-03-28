package com.pastime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pastime.SiteController;

@Configuration
public class ControllerConfig {

    @Bean
    public SiteController siteController() {
        return new SiteController();
    }
    
}