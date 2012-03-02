package org.springframework.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.CookieGenerator;

public class StringCookieGenerator {

    private final CookieGenerator cookieGenerator = new CookieGenerator();

    public StringCookieGenerator(String cookieName) {
        cookieGenerator.setCookieName(cookieName);
    }

    public void addCookie(String value, HttpServletResponse response) {
        cookieGenerator.addCookie(response, value);
    }

    public void removeCookie(HttpServletResponse response) {
        cookieGenerator.addCookie(response, "");
    }

    public String readCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieGenerator.getCookieName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
    
}
