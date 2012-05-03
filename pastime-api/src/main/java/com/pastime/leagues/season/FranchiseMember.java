package com.pastime.leagues.season;

class FranchiseMember {

    private Integer number;
    
    private String nickname;
    
    public FranchiseMember(Integer number, String nickname) {
        this.number = number;
        this.nickname = nickname;
    }

    public Integer getNumber() {
        return number;
    }

    public String getNickname() {
        return nickname;
    }

}
