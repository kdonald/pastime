package com.pastime.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.templating.JMustacheStringTemplateLoader;
import org.springframework.templating.StringTemplateLoader;

@Configuration
public class MailConfig {
    
    @Bean
    public StringTemplateLoader templateLoader(ResourceLoader resourceLoader) {
       JMustacheStringTemplateLoader loader = new JMustacheStringTemplateLoader(resourceLoader);
       // loader.setCache(false); // development only
       return loader;
    }

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("keith@pastimeconnect.com");
        mailSender.setPassword("whippet11!");
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }

}