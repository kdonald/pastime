package org.springframework.util;

import java.util.regex.Pattern;

/**
 * Email parsing/validation utilities.
 * @author Keith Donald
 */
public final class EmailUtils {

    /**
     * Returns true if the String is a valid email address.
     */
    public static boolean isEmail(String string) {
        return emailPattern.matcher(string).matches();
    }

    private static final Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

    private EmailUtils() {}

}
