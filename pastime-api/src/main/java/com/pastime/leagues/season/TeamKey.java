package com.pastime.leagues.season;

public final class TeamKey {
    
    private final Integer league;
    
    private final Integer season;
    
    private final Integer number;

    public TeamKey(Integer league, Integer season, Integer number) {
        this.league = league;
        this.season = season;
        this.number = number;
    }

    public Integer getLeagueId() {
        return league;
    }

    public Integer getSeasonId() {
        return season;
    }

    public Integer getNumber() {
        return number;
    }
    
}