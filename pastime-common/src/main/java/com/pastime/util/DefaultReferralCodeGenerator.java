package com.pastime.util;

import org.springframework.security.crypto.keygen.InsecureRandomStringGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

public class DefaultReferralCodeGenerator implements StringKeyGenerator, ReferralCodeGenerator {

    private final InsecureRandomStringGenerator generator = new InsecureRandomStringGenerator(6);

    public String generateKey() {
        return generator.generateKey();
    }
 
    public boolean meetsSyntax(String string) {
        return generator.meetsSyntax(string);
    }
    
}
