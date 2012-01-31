package com.pastime.prelaunch.referrals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.pastime.prelaunch.Name;
import com.pastime.prelaunch.Subscriber;
import com.pastime.prelaunch.Subscriber.ReferredBy;
import com.pastime.prelaunch.referrals.Referral;
import com.pastime.prelaunch.referrals.ReferralProgram;
import com.pastime.prelaunch.referrals.Referred;

@ContextConfiguration(classes=ReferralProgramTestsConfig.class)
@RunWith(value=SpringJUnit4ClassRunner.class)
public class ReferralProgramTests {

    @Inject
    private StringRedisTemplate redisTemplate;
    
    @Inject
    private ReferralProgram referralProgram;
        
    @Test
    public void subscriberAdded() {
        assertEquals((Integer) 0, referralProgram.getTotalReferrals());
        assertEquals(0, referralProgram.getAllReferrals().size());
        assertNull(referralProgram.getTotalReferrals("123456"));
        assertEquals(0, referralProgram.getReferred("123456").size());
        addSubscriber();
        assertEquals((Integer) 0, referralProgram.getTotalReferrals());
        assertEquals(0, referralProgram.getAllReferrals().size());
        assertEquals((Integer) 0, referralProgram.getTotalReferrals("123456"));
        assertEquals(0, referralProgram.getReferred("123456").size());
    }
    
    @Test
    public void referredByAdded() {
        assertEquals((Integer) 0, referralProgram.getTotalReferrals());
        assertEquals(0, referralProgram.getAllReferrals().size());
        assertNull(referralProgram.getTotalReferrals("123456"));
        assertEquals(0, referralProgram.getReferred("123456").size());
        addSubscriber();
        assertEquals((Integer) 0, referralProgram.getTotalReferrals());
        assertEquals(0, referralProgram.getAllReferrals().size());
        assertEquals((Integer) 0, referralProgram.getTotalReferrals("123456"));
        assertEquals(0, referralProgram.getReferred("123456").size());
        addReferredBy();
        assertEquals((Integer) 3, referralProgram.getTotalReferrals());
        List<Referral> referrals = referralProgram.getAllReferrals();
        assertEquals(3, referrals.size());
        assertEquals((Integer) 1, referralProgram.getTotalReferrals("123456"));
        assertEquals((Integer) 2, referralProgram.getTotalReferrals("234567"));
        List<Referred> referred = referralProgram.getReferred("123456");
        assertEquals(1, referred.size());
        assertEquals("January 24", referred.get(0).getDate());
        assertEquals("Keri D.", referred.get(0).getName());
        referred = referralProgram.getReferred("234567");        
        assertEquals(2, referred.size());
        assertEquals("January 26", referred.get(0).getDate());
        assertEquals("Corgan D.", referred.get(0).getName());
        assertEquals("January 25", referred.get(1).getDate());        
        assertEquals("Annabelle D.", referred.get(1).getName());        
    }
    
    public void addSubscriber() {
        Subscriber subscriber = new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date(1327417603859L));
        referralProgram.subscriberAdded(subscriber);
    }
    
    public void addReferredBy() {
        ReferredBy keith = new ReferredBy(1, new Name("Keith", "Donald"), "keith.donald@gmail.com", "123456");
        Subscriber keri = new Subscriber("keridonald@gmail.com", new Name("Keri", "Donald"), "234567", keith, new Date(1327417793928L));
        ReferredBy keriReferer = new ReferredBy(1, keri.getName(), keri.getEmail(), keri.getReferralCode());
        Subscriber annabelle = new Subscriber("annabelledonald@gmail.com", new Name("Annabelle", "Donald"), "345678", keriReferer, new Date(1327504500000L));
        Subscriber corgan = new Subscriber("corgandonald@gmail.com", new Name("Corgan", "Donald"), "456789", keriReferer, new Date(1327590900000L));
        referralProgram.subscriberAdded(keri);
        referralProgram.subscriberAdded(annabelle);
        referralProgram.subscriberAdded(corgan);        
    }

    @After
    public void cleanup() {
        redisTemplate.delete(redisTemplate.keys("referrals*"));
    }
}
