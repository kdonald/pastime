package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameTests {
    
    @Test
    public void publicDisplayName() {
        Name name = new Name("Keith", "Donald");
        assertEquals("Keith D.", name.getPublicDisplayName());
    }
    
}
