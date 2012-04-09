package com.pastime.leagues;

public final class TeamGender {
    
    public static final TeamGender MALE_ONLY = new TeamGender("m", "Men's");

    public static final TeamGender FEMALE_ONLY = new TeamGender("f", "Women's");

    public static final TeamGender CO_ED = new TeamGender("c", "Co-Ed");

    public static final TeamGender DOES_NOT_MATTER = new TeamGender(null, "");

    private final String value;
    
    private final String label;

    public TeamGender(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
    
    public static TeamGender valueOf(String value) {
        if (MALE_ONLY.value.equals(value)) {
            return MALE_ONLY;
        } else if (FEMALE_ONLY.value.equals(value)) {
            return FEMALE_ONLY;
        } else if (CO_ED.value.equals(value)) {
            return CO_ED;
        } else if (DOES_NOT_MATTER.value == value) {
            return DOES_NOT_MATTER;
        } else {
            throw new IllegalArgumentException("Unknown TeamGender value: " + value);
        }
    }
    
}