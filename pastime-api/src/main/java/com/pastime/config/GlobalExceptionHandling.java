package com.pastime.config;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;

class GlobalExceptionHandling {

    @ExceptionHandler(value = BindException.class)
    public void handle(BindException e, HttpServletRequest request,  HttpServletResponse response) throws IOException {
    	
    }
    
    @ExceptionHandler(value = IllegalArgumentException.class)
    public void handle(IllegalArgumentException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    }
    
    @ExceptionHandler(value = EmptyResultDataAccessException.class)
    public void handle(EmptyResultDataAccessException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    }

}
