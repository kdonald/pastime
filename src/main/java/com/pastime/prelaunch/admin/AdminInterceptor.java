package com.pastime.prelaunch.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

public final class AdminInterceptor extends HandlerInterceptorAdapter {

    private final AdminCookieGenerator adminCookieGenerator = new AdminCookieGenerator();
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("/admin".equals(request.getServletPath())) {
            if ("GET".equals(request.getMethod())) {
                return true;
            } else if ("POST".equals(request.getMethod())) {
                if ("Spik3rulez9".equals(request.getParameter("password"))) {
                    adminCookieGenerator.addCookie(response);
                }
                new RedirectView("/admin", true).render(null, request, response);
                return false;                
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return false;
            }
        }
        if (adminCookieGenerator.readCookieValue(request) || containsPassword(request)) {
            return true;
        } else {
            new RedirectView("/admin", true).render(null, request, response);            
            return false;
        }
    }
    
    private boolean containsPassword(HttpServletRequest request) {
        return "Spik3rulez9".equals(request.getParameter("password"));
    }

}
