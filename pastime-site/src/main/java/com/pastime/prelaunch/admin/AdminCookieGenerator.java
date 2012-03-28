package com.pastime.prelaunch.admin;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.CookieGenerator;

final class AdminCookieGenerator {

    private final CookieGenerator cookieGenerator = new CookieGenerator();

    public AdminCookieGenerator() {
        cookieGenerator.setCookieName("pastime_prelaunch_admin");
    }

    public void addCookie(HttpServletResponse response) {
        cookieGenerator.addCookie(response, "true");
    }

    public void removeCookie(HttpServletResponse response) {
        cookieGenerator.removeCookie(response);
    }

    public boolean readCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieGenerator.getCookieName())) {
                return Boolean.valueOf(cookie.getValue());
            }
        }
        return false;
    }

}
