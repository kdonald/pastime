package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.pastime.players.Name;

public class NameTests {
    
    @Test
    public void publicDisplayName() {
        Name name = new Name("Keith", "Donald");
        assertEquals("Keith D.", name.getPublicDisplayName());
        assertEquals("Keith Donald", name.toString());
    }
    
}
