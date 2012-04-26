package com.pastime.leagues.season;

public enum TeamMemberRole {
    
    PLAYER, COACH, ADMIN;

    public static String dbValue(TeamMemberRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        if (role == PLAYER) {
            return "p";
        } else if (role == COACH) {
            return "c";
        } else {
            return "a";
        }
    }
    
    public static TeamMemberRole dbValueOf(String role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        if (role.equals("p")) {
            return PLAYER;
        } else if (role.equals("c")) {
            return COACH;
        } else {
            return ADMIN;
        }
    }
    
}
