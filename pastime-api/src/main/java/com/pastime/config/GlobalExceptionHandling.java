package com.pastime.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;

class GlobalExceptionHandling {
    
    @ExceptionHandler(value = EmptyResultDataAccessException.class)
    public void handle(EmptyResultDataAccessException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.err.println(e);
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found");
    }

    @ExceptionHandler(value = BindException.class)
    public void handle(BindException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.err.println(e);        
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "data is invalid");
    }
    
    @ExceptionHandler(value = DuplicateKeyException.class)
    public void handle(DuplicateKeyException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.err.println(e);        
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "duplicates not allowed");
    }

}
