package com.pastime.leagues.season;

public final class TeamKey {
       
    private final SeasonKey season;
    
    private final Integer number;

    public TeamKey(SeasonKey season, Integer number) {
        this.season = season;
        this.number = number;
    }
    
    public TeamKey(Integer league, Integer season, Integer number) {
        this(new SeasonKey(league, season), number);
    }

    public Integer getLeague() {
        return season.getLeague();
    }
    
    public Integer getSeason() {
        return season.getNumber();
    }
    
    public Integer getNumber() {
        return number;
    }
    
}