package com.pastime.config;

import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.GlobalExceptionHandlerCapableExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.pastime.util.AuthorizedInterceptor;
import com.pastime.util.Principal;
import com.pastime.util.SecurityContext;
import com.pastime.util.SigninInterceptor;

@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SigninInterceptor(jdbcTemplate));
        registry.addInterceptor(new AuthorizedInterceptor());
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonConverter());
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new PrincipalMethodArgumentResolver());
    }
    
    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        GlobalExceptionHandlerCapableExceptionResolver resolver =
        		new GlobalExceptionHandlerCapableExceptionResolver(new GlobalExceptionHandling(), getMessageConverters());
        resolver.afterPropertiesSet();
        exceptionResolvers.add(resolver);
        exceptionResolvers.add(new ResponseStatusExceptionResolver());
        exceptionResolvers.add(new DefaultHandlerExceptionResolver());        
    }
    
    // internal helpers
    
    private static MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.registerModule(new JodaModule());
        converter.setObjectMapper(objectMapper);
        return converter;
    }
    
    private static class PrincipalMethodArgumentResolver implements HandlerMethodArgumentResolver {

        public boolean supportsParameter(MethodParameter parameter) {
            return Principal.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            return SecurityContext.getPrincipal();
        }
        
    }
}