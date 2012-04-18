package com.pastime.leagues.season;

import javax.validation.constraints.NotNull;

public class TeamForm {

    @NotNull
    private Integer league;
    
    @NotNull
    private Integer season;
    
    private String name;
    
    private Integer franchise;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFranchise() {
        return franchise;
    }

    public void setFranchise(Integer franchise) {
        this.franchise = franchise;
    }
    
    // TODO - look into JSR303 bean validation
    public void validateNameOrFranchiseSet() {
        if ((name == null || name.length() == 0) && franchise == null) {
            throw new IllegalStateException("Team name or franchise property must be set");
        }
    }
    
}
