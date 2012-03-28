package com.pastime.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringCookieGenerator;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


public class SigninInterceptor extends HandlerInterceptorAdapter {

    private StringCookieGenerator cookieGenerator = new StringCookieGenerator("auth_token");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String value = cookieGenerator.readCookieValue(request);
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
