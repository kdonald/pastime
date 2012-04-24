  package com.pastime.leagues.season;

public class SeasonKey {
    
    private Integer league;
    
    private Integer season;

    public SeasonKey(Integer league, Integer season) {
        this.league = league;
        this.season = season;
    }

    public Integer getLeague() {
        return league;
    }

    public Integer getNumber() {
        return season;
    }

}
