package com.pastime.prelaunch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pastime.util.DefaultReferralCodeGenerator;

public class ReferralCodeGeneratorTests {
    
    private DefaultReferralCodeGenerator generator = new DefaultReferralCodeGenerator();
    
    @Test
    public void generate() {
        for (int i = 0; i < 1000; i++) {
            String code = generator.generateKey();
            assertTrue(generator.meetsSyntax(code));            
        }
    }
    
    @Test
    public void not6() {
        assertFalse(generator.meetsSyntax("short"));
    }
    
    @Test
    public void notAlphanumeric() {
        assertFalse(generator.meetsSyntax("_12a45"));
    }
    
    @Test
    public void notLowercase() {
        assertFalse(generator.meetsSyntax("ABCdef"));
    }
    
}
