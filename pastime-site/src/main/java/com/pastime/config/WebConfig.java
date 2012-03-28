package com.pastime.config;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.GlobalExceptionHandlerCapableExceptionResolver;
import org.springframework.web.servlet.mvc.view.jmustache.JMustacheViewResolver;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Inject
    private BeanFactory beanFactory;
    
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.setOrder(0);
        registry.addResourceHandler("/channel.html").addResourceLocations("/channel.html").setCachePeriod(31556926);        
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/favicon.ico").setCachePeriod(2678400);
        registry.addResourceHandler("/static/app/0.1.2/**").addResourceLocations("/static/app/").setCachePeriod(31556926);
        registry.addResourceHandler("/static/libs/**").addResourceLocations("/static/libs/").setCachePeriod(31556926); // dev only
        registry.addResourceHandler("/static/*").addResourceLocations("/static/").setCachePeriod(31556926);
        // this is temporary until we do stored images right
        registry.addResourceHandler("/static/images/**").addResourceLocations("/static/images/").setCachePeriod(0);        

    }

    @Bean
    public ViewResolver viewResolver(ResourceLoader resourceLoader) {
        JMustacheViewResolver resolver = new JMustacheViewResolver(resourceLoader);
        resolver.setCache(false); // development only
        return resolver;
    }
    
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        GlobalExceptionHandlerCapableExceptionResolver resolver = new GlobalExceptionHandlerCapableExceptionResolver(new GlobalExceptionHandling(), getMessageConverters());
        resolver.afterPropertiesSet();
        exceptionResolvers.add(resolver);
    }
    
    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/errors/bad-request");
        registry.addViewController("/errors/resource-not-found");
        registry.addViewController("/errors/internal-error");    
        super.addViewControllers(registry);
    }
    
    @PostConstruct
    public void reorderMappings() {
        RequestMappingHandlerMapping controllers = beanFactory.getBean(RequestMappingHandlerMapping.class);
        controllers.setOrder(1);
    }
    
}