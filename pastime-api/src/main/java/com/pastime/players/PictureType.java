package com.pastime.players;

public final class PictureType {
    
    public static final PictureType SMALL = new PictureType("small");
    
    private final String value;
    
    public String getValue() {
        return value;
    }

    private PictureType(String value) {
        this.value = value;
    }
    
    public PictureType valueOf(String value) {
        if (SMALL.value.equalsIgnoreCase(value)) {
            return SMALL;
        } else {
            throw new IllegalArgumentException("Unknown picture type: " + value);
        }
    }
    
    public String toString() {
        return getValue();
    }
}
