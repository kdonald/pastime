package com.pastime.players;

public enum Gender {
    MALE, FEMALE;

    public static Gender dbValueOf(String value) {
        if ("m".equals(value)) {
            return MALE;
        } else if ("f".equals(value)) {
            return FEMALE;
        } else {
            throw new IllegalArgumentException(value);
        }
    }
    
}
