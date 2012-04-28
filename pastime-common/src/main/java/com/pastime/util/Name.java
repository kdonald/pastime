package com.pastime.util;

import java.util.ArrayList;
import java.util.List;

public class Name {
    
    private final String firstName;
    
    private final String lastName;

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

    public String getPublicDisplayName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getFirstName());
        if (getLastName() != null && getLastName().length() > 0) {
            builder.append(' ').append(getLastName().charAt(0)).append(".");
        }
        return builder.toString();
    }

    public String toString() {
        return firstName + " " + lastName;
    }

	public static Name valueOf(String name) {
		if (name == null) {
			return null;
		}
		name = name.trim();
		if (name.length() == 0) {
			return null;
		}
        if (isFirstNameOnly(name)) {
            return new Name(name, null);
        }
        int pos = findWordStart(name, name.length() - 1);
        if (isSuffix(name.substring(pos))) {
            pos = findWordStart(name, pos);
        }
        return new Name(name.substring(0, pos).trim(), name.substring(pos).trim());
	}

	public static Name valueOf(String firstName, String lastName) {
		if (firstName == null && lastName == null) {
			return null;
		}
		if (firstName != null) {
			firstName = firstName.trim();
			if (firstName.length() == 0) {
				firstName = null;
			}
		}
		if (lastName != null) {
			lastName = lastName.trim();
			if (lastName.length() == 0) {
				lastName = null;
			}
		}
		if (firstName == null && lastName == null) {
			return null;
		}
		return new Name(firstName, lastName);
	}
    
    private static boolean isFirstNameOnly(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (Character.isWhitespace(name.charAt(i))) {
                return false;
            }
        }
        return true;        
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
}
