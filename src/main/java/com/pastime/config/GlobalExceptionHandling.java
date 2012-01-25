package com.pastime.config;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;

class GlobalExceptionHandling {

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<ErrorBody> handle(BindException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ErrorBody body = new ErrorBody(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<ErrorBody>(body, headers, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(value = IllegalArgumentException.class)
    public void handle(IllegalArgumentException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    public static class ErrorBody {

        private final String errorMessage;

        public ErrorBody(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

    }

}
