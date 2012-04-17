package com.pastime.franchises;

import org.joda.time.LocalDate;

public class PlayerMemberRole extends FranchiseMemberRole {

    private final boolean captain;
    
    private final String captainOf;
    
    public PlayerMemberRole(LocalDate became, LocalDate retired, boolean captain, String captainOf) {
        super(became, retired);
        this.captain = captain;
        this.captainOf = captainOf;
    }
    
    public boolean isCaptain() {
        return captain;
    }
    
    public String getCaptainOf() {
        return captainOf;
    }
    
}
