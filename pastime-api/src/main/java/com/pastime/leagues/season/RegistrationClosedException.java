package com.pastime.leagues.season;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="registration closed")
public class RegistrationClosedException extends RuntimeException {

    private Integer league;
    
    private Integer season;

    public RegistrationClosedException(Integer league, Integer season) {
        super("Registration closed");
        this.league = league;
        this.season = season;
    }

    public Integer getLeague() {
        return league;
    }

    public Integer getSeason() {
        return season;
    }
    
}
