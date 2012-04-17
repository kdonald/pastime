package com.pastime.franchises;

public final class MemberRole {

    public static final MemberRole ALL = new MemberRole("All");

    public static final MemberRole PLAYER = new MemberRole("Player");

    public static final MemberRole ADMIN = new MemberRole("Admin");

    public static final MemberRole COACH = new MemberRole("Coach");

    private final String value;
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return value;
    }

    public static MemberRole valueOf(String value) {
        if (PLAYER.value.equalsIgnoreCase(value)) {
            return PLAYER;
        } else if (COACH.value.equalsIgnoreCase(value)) {
            return COACH;
        } else if (ADMIN.value.equalsIgnoreCase(value)) {
            return ADMIN;
        } else if (ALL.value.equalsIgnoreCase(value)) { 
            return ALL;
        } else {
            throw new IllegalArgumentException("Not a valid member role: " + value);
        }
    }
    
    private MemberRole(String value) {
        this.value = value;
    }
    
}