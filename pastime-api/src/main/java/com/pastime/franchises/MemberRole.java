package com.pastime.franchises;

public enum MemberRole {
    ALL, PLAYER, ADMIN, COACH;

    public static String dbValue(MemberRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        if (role == ALL) {
            throw new IllegalArgumentException("ALL is a filter value and not stored in the database");
        }
        if (role == PLAYER) {
            return "p";
        } else if (role == COACH) {
            return "c";
        } else {
            return "a";
        }
    }
    
}