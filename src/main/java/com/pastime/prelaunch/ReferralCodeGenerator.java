package com.pastime.prelaunch;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

public class ReferralCodeGenerator implements StringKeyGenerator {

    private final Base64 base64 = new Base64(true);
    
    private final BytesKeyGenerator generator = KeyGenerators.secureRandom(4);
    
    // implementing StringKeyGenerator

    public String generateKey() {
        return new String(base64.encode(generator.generateKey())).trim();
    }
    
}
