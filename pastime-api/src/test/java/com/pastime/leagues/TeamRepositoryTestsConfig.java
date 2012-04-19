package com.pastime.leagues;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.templating.JMustacheStringTemplateLoader;
import org.springframework.templating.StringTemplateLoader;

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
    public JavaMailSender mailSender() {
        return new JavaMailSenderImpl();
    }
    
    @Bean
    public StringTemplateLoader templateLoader() {
        JMustacheStringTemplateLoader templateLoader = new JMustacheStringTemplateLoader(new FileSystemResourceLoader());
        templateLoader.setPrefix("src/main/webapp/WEB-INF/views/");
        return templateLoader;
    }
    
    @Bean
    public TeamRepository teamRepository() {
        return new TeamRepository(jdbcTemplate, mailSender(), templateLoader(), environment);
    }
    
}