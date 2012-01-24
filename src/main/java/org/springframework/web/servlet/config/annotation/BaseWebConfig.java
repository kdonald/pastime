package org.springframework.web.servlet.config.annotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class BaseWebConfig extends WebMvcConfigurationSupport {

    private WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();

    @Autowired(required = false)
    public void setConfigurers(List<WebMvcConfigurer> configurers) {
        if (configurers == null || configurers.isEmpty()) {
            return;
        }
        this.configurers.addWebMvcConfigurers(configurers);
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        configurers.addInterceptors(registry);
    }

    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        configurers.addViewControllers(registry);
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        configurers.addResourceHandlers(registry);
    }

    @Override
    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurers.configureDefaultServletHandling(configurer);
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        configurers.addArgumentResolvers(argumentResolvers);
    }

    @Override
    protected void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        configurers.addReturnValueHandlers(returnValueHandlers);
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        configurers.configureMessageConverters(converters);
    }

    @Override
    protected void addFormatters(FormatterRegistry registry) {
        configurers.addFormatters(registry);
    }

    @Override
    protected Validator getValidator() {
        return configurers.getValidator();
    }

    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        configurers.configureHandlerExceptionResolvers(exceptionResolvers);
    }

}
