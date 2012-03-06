package com.pastime.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;

class GlobalExceptionHandling {

    @ExceptionHandler(value = BindException.class)
    public void handle(BindException e, HttpServletRequest request,  HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    @ExceptionHandler(value = IllegalArgumentException.class)
    public void handle(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        System.err.print(e);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

}
