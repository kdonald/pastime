package com.pastime.util;

public class ErrorReporter {
    
    private String message;
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;        
    }
    
    public void clear() {
        this.message = null;
    }
}
