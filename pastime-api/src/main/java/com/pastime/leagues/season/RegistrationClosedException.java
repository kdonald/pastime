package com.pastime.leagues.season;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="registration closed")
public class RegistrationClosedException extends RuntimeException {

    private SeasonKey season;
    
    public RegistrationClosedException(SeasonKey season) {
        super("Registration closed");
        this.season = season;
    }

    public SeasonKey getSeason() {
        return season;
    }
    
}
