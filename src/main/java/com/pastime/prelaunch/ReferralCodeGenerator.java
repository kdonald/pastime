package com.pastime.prelaunch;

import org.springframework.security.crypto.keygen.InsecureRandomStringGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

public class ReferralCodeGenerator implements StringKeyGenerator {

    private final InsecureRandomStringGenerator generator = new InsecureRandomStringGenerator(6);

    public String generateKey() {
        return generator.generateKey();
    }
 
    public boolean meetsSyntax(String string) {
        if (string.length() != 6) {
            return false;
        }
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!(Character.getType(c) == Character.LOWERCASE_LETTER || Character.isDigit(c))) {
                return false;
            }
        }
        return true;
    }
    
}
