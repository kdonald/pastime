package com.pastime.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringCookieGenerator;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class SigninInterceptor extends HandlerInterceptorAdapter {

    private StringCookieGenerator cookieGenerator = new StringCookieGenerator("access_token");

    private boolean cookie;
    
    private JdbcTemplate jdbcTemplate;
    
    public SigninInterceptor(JdbcTemplate jdbcTemplate) {
    	this.jdbcTemplate = jdbcTemplate;
    }
    
    public void setCookie(boolean cookie) {
    	this.cookie = cookie;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = readToken(request);   	
        if (token != null && token.length() > 0) {
            SecurityContext.setCurrentPlayer(findPlayer(token));            
        } else {
            SecurityContext.setCurrentPlayer(null);
        }    	
        return true;
    }

    private String readToken(HttpServletRequest request) {
    	if (cookie) {
    		return cookieGenerator.readCookieValue(request);     		
    	} else {
    		String value = request.getHeader("Authorization");
    		if (value == null) {
    			value = request.getParameter("access_token");
    		}
    		return value;
    	}    	
    }
    
    private PlayerPrincipal findPlayer(String accessToken) {
        Integer playerId = Integer.parseInt(accessToken);
        return DataAccessUtils.singleResult(jdbcTemplate.query("SELECT id FROM players WHERE id = ?", new RowMapper<PlayerPrincipal>() {
			public PlayerPrincipal mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return new PlayerPrincipal(rs.getInt("id"));
			}
        	
        }, playerId));
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SecurityContext.remove();
    }
        
}