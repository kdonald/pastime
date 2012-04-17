package com.pastime.leagues.season;

public final class TeamMemberRole {

    public static final TeamMemberRole ADMIN = new TeamMemberRole("Admin");
    
    public static final TeamMemberRole HEAD_COACH = new TeamMemberRole("Head Coach");

    public static final TeamMemberRole ASSISTANT_COACH = new TeamMemberRole("Assistant Coach");

    public static final TeamMemberRole PLAYER = new TeamMemberRole("Player");
    
    private String value;
    
    private TeamMemberRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
}
