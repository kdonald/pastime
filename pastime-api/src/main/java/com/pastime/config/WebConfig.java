package com.pastime.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.support.GlobalExceptionHandlerCapableExceptionResolver;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        GlobalExceptionHandlerCapableExceptionResolver resolver =
        		new GlobalExceptionHandlerCapableExceptionResolver(new GlobalExceptionHandling(), getMessageConverters());
        resolver.afterPropertiesSet();
        exceptionResolvers.add(resolver);
    }
    
}