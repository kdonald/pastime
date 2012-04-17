package com.pastime.franchises;

public final class MemberStatus {

    public static final MemberStatus ALL = new MemberStatus("all");
    
    public static final MemberStatus CURRENT = new MemberStatus("current");

    public static final MemberStatus RETIRED = new MemberStatus("retired");

    private final String value;
    
    private MemberStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }

    public static MemberStatus valueOf(String value) {
        if (ALL.value.equalsIgnoreCase(value)) {
            return ALL;
        } else if (CURRENT.value.equalsIgnoreCase(value)) {
            return CURRENT;
        } else if (RETIRED.value.equalsIgnoreCase(value)) {
            return RETIRED;
        } else {
            throw new IllegalArgumentException("Not a valid member status: " + value);
        }
    }
    
}
