package com.pastime.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.templating.StringTemplateLoader;

import com.pastime.next.NextController;

@Configuration
public class NextConfig {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JavaMailSender mailSender;

    @Inject
    private StringTemplateLoader templateLoader;

    @Bean
    public NextController nextController() {
        return new NextController(jdbcTemplate, mailSender, templateLoader);
    }

}
