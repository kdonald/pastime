package com.pastime.config;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.servlet.mvc.view.jmustache.JMustacheViewResolver;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.BaseWebConfig;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

@Configuration
public class WebConfig extends BaseWebConfig {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/channel.html").addResourceLocations("/channel.html").setCachePeriod(31556926);        
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/favicon.ico").setCachePeriod(2678400);
        registry.addResourceHandler("/static/app/0.1.2/**").addResourceLocations("/static/app/").setCachePeriod(31556926);
        registry.addResourceHandler("/static/libs/**").addResourceLocations("/static/libs/").setCachePeriod(31556926); // dev only
        registry.addResourceHandler("/static/*").addResourceLocations("/static/").setCachePeriod(31556926);
    }

    @Bean
    public ViewResolver viewResolver(ResourceLoader resourceLoader) {
        JMustacheViewResolver resolver = new JMustacheViewResolver(resourceLoader);
        resolver.setCache(false); // development only
        return resolver;
    }
    
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        GlobalExceptionHandlerCapableExceptionResolver resolver = new GlobalExceptionHandlerCapableExceptionResolver(new GlobalExceptionHandling());
        resolver.afterPropertiesSet();
        exceptionResolvers.add(resolver);
    }
    
    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/errors/bad-request");
        registry.addViewController("/errors/resource-not-found");
        registry.addViewController("/errors/internal-error");    
        // TODO split into module once large enough
        registry.addViewController("/teams/player");
        registry.addViewController("/leagues/preview");
        super.addViewControllers(registry);
    }

    // temp extension for enabling Global @ExceptionHandlers until Spring MVC supports this by default (likely 3.2)
    private class GlobalExceptionHandlerCapableExceptionResolver extends ExceptionHandlerExceptionResolver {

        private final Object handling;

        private final ExceptionHandlerMethodResolver methodResolver;

        public GlobalExceptionHandlerCapableExceptionResolver(Object handling) {
            this.handling = handling;
            this.methodResolver = new ExceptionHandlerMethodResolver(handling.getClass());
            setMessageConverters(WebConfig.this.getMessageConverters());
        }

        @Override
        protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
            ServletInvocableHandlerMethod result = super.getExceptionHandlerMethod(handlerMethod, exception);
            if (result != null) {
                return result;
            }
            Method method = this.methodResolver.resolveMethod(exception);
            return (method != null) ? new ServletInvocableHandlerMethod(this.handling, method) : null;
        }

    }
    
}