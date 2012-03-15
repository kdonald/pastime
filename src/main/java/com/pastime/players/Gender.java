package com.pastime.players;

public final class Gender {
    
    public static final Gender MALE = new Gender('m', "Male");

    public static final Gender FEMALE = new Gender('f', "Female");

    private final char code;
    
    private final String label;

    public char getCode() {
        return code;
    }
    
    public String getLabel() {
        return label;
    }
    
    public static Gender valueOf(String string) {
        if (string == null) {
            return null;
        }
        string = string.trim();
        if (string.length() == 1) {
            return forCode(string.charAt(0));
        } else {
            return forLabel(string);
        }
    }
    
    private static Gender forCode(char code) {
        if (MALE.code == code) {
            return MALE;
        } else if (FEMALE.code == code) {
            return FEMALE;
        } else {
            throw new IllegalArgumentException("Invalid Gender character code: '" + code + "'");
        }
    }
    
    private static Gender forLabel(String string) {
        if (MALE.label.equalsIgnoreCase(string)) {
            return MALE;
        } else if (FEMALE.label.equalsIgnoreCase(string)) {
            return FEMALE;
        } else {
            throw new IllegalArgumentException("Invalid Gender label: '" + string + "'");
        }        
    }
    
    private Gender(char code, String label) {
        this.code = code;
        this.label = label;
    }
   
}
