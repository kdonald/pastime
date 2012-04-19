package com.pastime.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class AuthorizedInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	HandlerMethod hm = (HandlerMethod) handler;
    	if (hm.getMethodAnnotation(Authorized.class) != null && SecurityContext.getPrincipal() == null) {
    		response.sendError(HttpServletResponse.SC_FORBIDDEN, "not authorized");
    		return false;
    	} else {    	
    		return true;
    	}
    }
       
}