package com.pastime.leagues.season;

public class FranchiseMember {

    private String franchiseName;
    
    private Integer number;
    
    private String nickname;
    
    public FranchiseMember(String franchiseName, Integer number, String nickname) {
        this.franchiseName = franchiseName;
        this.number = number;
        this.nickname = nickname;
    }

    public String getFranchiseName() {
        return franchiseName;
    }

    public Object getNumber() {
        return number;
    }

    public Object getNickname() {
        return nickname;
    }

}
