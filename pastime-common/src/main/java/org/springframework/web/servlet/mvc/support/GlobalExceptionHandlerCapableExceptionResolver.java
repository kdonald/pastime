package org.springframework.web.servlet.mvc.support;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

// temp extension for enabling Global @ExceptionHandlers until Spring MVC supports this by default (likely 3.2)
public class GlobalExceptionHandlerCapableExceptionResolver extends ExceptionHandlerExceptionResolver {

    private final Object handling;

    private final ExceptionHandlerMethodResolver methodResolver;

    public GlobalExceptionHandlerCapableExceptionResolver(Object handling, List<HttpMessageConverter<?>> converters) {
        this.handling = handling;
        this.methodResolver = new ExceptionHandlerMethodResolver(handling.getClass());
        setMessageConverters(converters);
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