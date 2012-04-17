package com.pastime.franchises;

import org.joda.time.LocalDate;

public class FranchiseMemberRole {

    private final LocalDate became;
    
    private final LocalDate retired;

    public FranchiseMemberRole(LocalDate became, LocalDate retired) {
        this.became = became;
        this.retired = retired;
    }

    public LocalDate getBecame() {
        return became;
    }

    public LocalDate getRetired() {
        return retired;
    }    
    
}
