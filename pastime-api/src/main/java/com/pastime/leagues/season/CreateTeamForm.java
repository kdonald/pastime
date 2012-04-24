package com.pastime.leagues.season;

public class CreateTeamForm {

    private String name;
    
    private Integer franchise;

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
            throw new IllegalArgumentException("Team name or franchise property must be set");
        }
    }
    
}
