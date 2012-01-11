package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

public class SubscribeTests {

    @Test
    public void referralCodeGenerator() {
        StringKeyGenerator generator = new ReferralCodeGenerator();
        String code = generator.generateKey().trim();
        assertEquals(6, code.length());
    }
    
    @Test
    public void names() {
        Name name = Name.parseName("Keith Donald");
        assertEquals("Keith", name.getFirstName());
        assertEquals("Donald", name.getLastName());

        name = Name.parseName("Keith P. Donald");
        assertEquals("Keith P.", name.getFirstName());
        assertEquals("Donald", name.getLastName());

        name = Name.parseName("Keith Preston Donald");
        assertEquals("Keith Preston", name.getFirstName());
        assertEquals("Donald", name.getLastName());

        name = Name.parseName("Bill Donald III");
        assertEquals("Bill", name.getFirstName());
        assertEquals("Donald III", name.getLastName());
    }
}
