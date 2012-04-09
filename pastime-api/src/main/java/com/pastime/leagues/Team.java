package com.pastime.leagues;

public class Team {
    
    private Integer league;
    
    private Integer season;
    
    private Integer number;
    
    private Roster roster;

    public Team(Integer league, Integer season, Integer number, Roster roster) {
        this.league = league;
        this.season = season;
        this.number = number;
        this.roster = roster;
    }

    public Integer getLeague() {
        return league;
    }

    public Integer getSeason() {
        return season;
    }

    public Integer getNumber() {
        return number;
    }

    public Roster getRoster() {
        return roster;
    }
    
}