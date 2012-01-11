package com.pastime.prelaunch;

import java.util.ArrayList;
import java.util.List;

public class Name {
    
    private String firstName;
    
    private String lastName;

    public Name(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    
    public static final Name parseName(String name) {
        int pos = findWordStart(name, name.length() - 1);
        if (isSuffix(name.substring(pos))) {
            pos = findWordStart(name, pos);
        }
        return new Name(name.substring(0, pos).trim(), name.substring(pos).trim());
    }
    
    private static int findWordStart(String name, int startIndex) {
        while (startIndex > 0 && Character.isWhitespace(name.charAt(startIndex))) {
            startIndex --;
        }
        while (startIndex > 0 && !Character.isWhitespace(name.charAt(startIndex))) {
            startIndex--;
        }
        return startIndex;
    }

    static final List<String> SUFFIXES = new ArrayList<String>();
    
    static {
        // Initialize suffixes
        SUFFIXES.add("jr");
        SUFFIXES.add("sr");
        SUFFIXES.add("esq");
        SUFFIXES.add("ii");
        SUFFIXES.add("iii");
        SUFFIXES.add("iv");
        SUFFIXES.add("v");
        SUFFIXES.add("2nd");
        SUFFIXES.add("3rd");
        SUFFIXES.add("4th");
        SUFFIXES.add("5th");
    }
    
    private static boolean isSuffix(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i))) {
                sb.append(s.charAt(i));
            }
        }
        return SUFFIXES.contains(sb.toString().toLowerCase());
    }
    
    public String toString() {
        return "[Name firstName='" + getFirstName() + "' lastName='" + getLastName() + "']";
    }
}
