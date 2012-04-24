package com.pastime.leagues.season;

import javax.validation.constraints.NotNull;

public abstract class SeasonForm {

    @NotNull
    private Integer league;
    
    @NotNull
    private Integer season;

    public Integer getLeague() {
        return league;
    }

    public void setLeague(Integer league) {
        this.league = league;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

}
