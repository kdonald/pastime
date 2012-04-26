package com.pastime.util;


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
		return null;
	}

	public static Name valueOf(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
	}

}
