package com.pastime.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringCookieGenerator;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class SigninInterceptor extends HandlerInterceptorAdapter {

    private StringCookieGenerator cookieGenerator = new StringCookieGenerator("access_token");

    private boolean cookie;
    
    public void setCookie(boolean cookie) {
    	this.cookie = cookie;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String value;    	
    	if (cookie) {
    		value = cookieGenerator.readCookieValue(request);     		
    	} else {
    		value = request.getHeader("Authorization");
    		if (value == null) {
    			value = request.getParameter("access_token");
    		}
    	}
        if (value != null && value.length() > 0) {
            Integer playerId = Integer.parseInt(value);
            PlayerPrincipal player = new PlayerPrincipal(playerId);
            SecurityContext.setCurrentPlayer(player);            
        } else {
            SecurityContext.setCurrentPlayer(null);
        }    	
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SecurityContext.remove();
    }
    
    
    
    
}
