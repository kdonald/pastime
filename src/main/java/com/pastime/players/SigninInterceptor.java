package com.pastime.players;

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
            Player player = new Player(playerId);
            SecurityContext.setCurrentPlayer(player);            
        }
        return true;
    }
    
    
}