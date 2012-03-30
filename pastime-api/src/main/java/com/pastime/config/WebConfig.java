package com.pastime.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.support.GlobalExceptionHandlerCapableExceptionResolver;

import com.pastime.util.SigninInterceptor;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SigninInterceptor());
    }
    
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        GlobalExceptionHandlerCapableExceptionResolver resolver =
        		new GlobalExceptionHandlerCapableExceptionResolver(new GlobalExceptionHandling(), getMessageConverters());
        resolver.afterPropertiesSet();
        exceptionResolvers.add(resolver);
    }
    
}