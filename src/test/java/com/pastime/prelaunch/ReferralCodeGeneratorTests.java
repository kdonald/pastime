package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReferralCodeGeneratorTests {
    
    private ReferralCodeGenerator generator = new ReferralCodeGenerator();
    
    @Test
    public void generate() {
        for (int i = 0; i < 1000; i++) {
            String code = generator.generateKey();
            assertEquals(6, code.length());
            for (int j = 0; j < 5; j++) {
                char c = code.charAt(j);
                assertTrue(Character.isAlphabetic(c) && Character.isLowerCase(c) || Character.isDigit(c));
            }
        }
    }
}
