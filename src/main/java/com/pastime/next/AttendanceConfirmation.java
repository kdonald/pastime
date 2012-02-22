package com.pastime.next;

public class AttendanceConfirmation {
    
    private String name;
    
    private boolean attending;

    public AttendanceConfirmation(String name, boolean attending) {
        this.name = name;
        this.attending = attending;
    }

    public String getName() {
        return name;
    }

    public boolean isAttending() {
        return attending;
    }
    
}
