package com.pastime.prelaunch;

import org.springframework.security.crypto.keygen.InsecureRandomStringGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

public class ReferralCodeGenerator implements StringKeyGenerator {

    private final InsecureRandomStringGenerator generator = new InsecureRandomStringGenerator(6);

    public String generateKey() {
        return generator.generateKey();
    }
    
}
