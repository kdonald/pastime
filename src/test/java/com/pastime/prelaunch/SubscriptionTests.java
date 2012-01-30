package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(classes=SubscriptionTestsConfig.class)
@Transactional
@RunWith(value=SpringJUnit4ClassRunner.class)
public class SubscriptionTests {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Inject
    private SubscriptionRepository controller;

    private ReferralCodeGenerator referralCodeGenerator = Mockito.mock(ReferralCodeGenerator.class);
    
    private SubscriberListener subscriberListener = Mockito.mock(SubscriberListener.class);
    
    @Before
    public void setupDefaults() {
        Mockito.when(referralCodeGenerator.generateKey()).thenReturn("123456");
        controller.setReferralCodeGenerator(referralCodeGenerator);
        controller.setSubscriberListener(subscriberListener);
    }
    
    @Test
    public void subscribe() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        Subscription subscription = controller.subscribe(form);        
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date()));
        assertEquals(subscription.getFirstName(), "Keith");
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keith.donald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keith", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("123456", row.get("referral_code"));
        assertNull(row.get("referred_by"));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));
    }

    @Test
    public void subscribeAgain() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        Subscription subscription = controller.subscribe(form);        
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date()));
        assertEquals(subscription.getFirstName(), "Keith");        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        subscription = controller.subscribe(form);
        Mockito.verifyNoMoreInteractions(subscriberListener);
        assertEquals(subscription.getFirstName(), "Keith");        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));        
    }

    @Test
    public void subscribeReferredBy() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        Subscription subscription = controller.subscribe(form);

        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keridonald@gmail.com')", Boolean.class));
        form = new SubscribeForm();
        form.setFirstName("Keri");
        form.setLastName("Donald");
        form.setEmail("keridonald@gmail.com");
        form.setR("123456");
        Mockito.when(referralCodeGenerator.generateKey()).thenReturn("234567");
        controller.setSubscriberListener(new SubscriberListener() {
            public void subscriberAdded(Subscriber subscriber) {
                assertEquals("123456", subscriber.getReferredBy().getReferralCode());
            }
        });
        subscription = controller.subscribe(form);
        assertEquals(subscription.getFirstName(), "Keri");    
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=234567", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keridonald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keridonald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keri", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("234567", row.get("referral_code"));
        assertEquals("123456", jdbcTemplate.queryForObject("select referral_code from prelaunch.subscriptions where id = ?", String.class, row.get("referred_by")));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));        
    }
    
    @Test
    public void subscribeReferredByInvalidReferralCode() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        form.setR("234567");
        Subscription subscription = controller.subscribe(form);
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date()));
        assertEquals(subscription.getFirstName(), "Keith");        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());        

        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keith.donald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keith", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("123456", row.get("referral_code"));
        assertNull(row.get("referred_by"));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));        
    }
    
    @Test
    public void subscribeReferredByGarbageReferralCode() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        form.setR("<html><head><title></title></head><body><script>alert('hello');</script></body></html>");
        Subscription subscription = controller.subscribe(form);
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date()));
        assertEquals(subscription.getFirstName(), "Keith");        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());        

        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keith.donald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keith", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("123456", row.get("referral_code"));
        assertNull(row.get("referred_by"));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));        
    }

    @Test
    public void subscribeReferredByCaseInsensitive() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        Mockito.when(referralCodeGenerator.generateKey()).thenReturn("a3c2b5");        
        Subscription subscription = controller.subscribe(form);
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "a3c2b5", null, new Date()));
        assertEquals(subscription.getFirstName(), "Keith");        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=a3c2b5", subscription.getReferralLink());        

        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keridonald@gmail.com')", Boolean.class));
        form = new SubscribeForm();
        form.setFirstName("Keri");
        form.setLastName("Donald");
        form.setEmail("keridonald@gmail.com");
        form.setR("A3c2B5");
        Mockito.when(referralCodeGenerator.generateKey()).thenReturn("234567");
        controller.setSubscriberListener(new SubscriberListener() {
            public void subscriberAdded(Subscriber subscriber) {
                assertNotNull(subscriber.getReferredBy());
                assertEquals("a3c2b5", subscriber.getReferredBy().getReferralCode());
            }
        });
        subscription = controller.subscribe(form);
        assertEquals(subscription.getFirstName(), "Keri");    
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=234567", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keridonald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keridonald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keri", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("234567", row.get("referral_code"));
        assertEquals("a3c2b5", jdbcTemplate.queryForObject("select referral_code from prelaunch.subscriptions where id = ?", String.class, row.get("referred_by")));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));        
    }

    @Test
    public void subscribeWhitespace() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("   Keith     ");
        form.setLastName("Donald            ");
        form.setEmail("keith.donald@gmail.com      ");
        Subscription subscription = controller.subscribe(form);
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date()));
        assertEquals(subscription.getFirstName(), "Keith");        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));

        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keridonald@gmail.com')", Boolean.class));
        form = new SubscribeForm();
        form.setFirstName("Keri      ");
        form.setLastName("       Donald");
        form.setEmail("keridonald@gmail.com ");
        form.setR(" 123456");
        Mockito.when(referralCodeGenerator.generateKey()).thenReturn("234567");
        controller.setSubscriberListener(new SubscriberListener() {
            public void subscriberAdded(Subscriber subscriber) {
                assertNotNull(subscriber.getReferredBy());
                assertEquals("123456", subscriber.getReferredBy().getReferralCode());
            }
        });
        subscription = controller.subscribe(form);
        assertEquals(subscription.getFirstName(), "Keri");    
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=234567", subscription.getReferralLink());        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keridonald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keridonald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keri", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("234567", row.get("referral_code"));
        assertEquals("123456", jdbcTemplate.queryForObject("select referral_code from prelaunch.subscriptions where id = ?", String.class, row.get("referred_by")));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));
    }
    
    @Test(expected=DataIntegrityViolationException.class)
    public void subscribeMissingEmail() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        controller.subscribe(form);
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
    }
    
    @Test
    public void subscribeDuplicateReferralCode() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith@pastimebrevard.com')", Boolean.class));        
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        Subscription subscription = controller.subscribe(form);
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date()));
        assertEquals("http://pastimebrevard.com/?r=123456", subscription.getReferralLink());
        form.setEmail("keith@pastimebrevard.com");
        controller.setReferralCodeGenerator(new DuplicateReturningReferralCodeGenerator());
        subscription = controller.subscribe(form);
        Mockito.verify(subscriberListener).subscriberAdded(new Subscriber("keith@pastimebrevard.com", new Name("Keith", "Donald"), "234567", null, new Date()));        
        assertNotNull(subscription.getReferralLink());
        assertEquals("http://pastimebrevard.com/?r=234567", subscription.getReferralLink());
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith@pastimebrevard.com')", Boolean.class));
    }

    @Test
    public void unsubscribe() {
        // subscribe first
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        SubscribeForm form = new SubscribeForm();
        form.setFirstName("Keith");
        form.setLastName("Donald");
        form.setEmail("keith.donald@gmail.com");
        controller.subscribe(form);        
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        Map<String, Object> row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keith.donald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keith", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("123456", row.get("referral_code"));
        assertNull(row.get("referred_by"));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));
        // unsubscribe
        UnsubscribeForm unsubscribeForm = new UnsubscribeForm();
        unsubscribeForm.setEmail("keith.donald@gmail.com  ");
        controller.unsubscribe(unsubscribeForm.getEmail());
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));        
        row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keith.donald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keith", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("123456", row.get("referral_code"));
        assertNull(row.get("referred_by"));
        assertNotNull(row.get("created"));
        assertTrue((Boolean) row.get("unsubscribed"));
        controller.subscribe(form);
        // subscribe again
        assertTrue(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));
        row = jdbcTemplate.queryForMap("select id, first_name, last_name, referral_code, referred_by, created, unsubscribed from prelaunch.subscriptions where email = 'keith.donald@gmail.com'");
        assertNotNull(row.get("id"));
        assertEquals("Keith", row.get("first_name"));
        assertEquals("Donald", row.get("last_name"));
        assertEquals("123456", row.get("referral_code"));
        assertNull(row.get("referred_by"));
        assertNotNull(row.get("created"));
        assertFalse((Boolean) row.get("unsubscribed"));        
    }
    
    @Test
    public void unsubscribeNeverSubscribed() {
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));        
        UnsubscribeForm unsubscribeForm = new UnsubscribeForm();
        unsubscribeForm.setEmail("keith.donald@gmail.com");
        controller.unsubscribe(unsubscribeForm.getEmail());
        assertFalse(jdbcTemplate.queryForObject("select exists(select 1 from prelaunch.subscriptions where email = 'keith.donald@gmail.com')", Boolean.class));        
    }
    
    private static class DuplicateReturningReferralCodeGenerator implements ReferralCodeGenerator {
        
        private int timesInvoked = 0;

        public String generateKey() {
            timesInvoked++;
            if (timesInvoked == 3) {
                return "234567";
            } else {
                return "123456";
            }
        }

        public boolean meetsSyntax(String string) {
            return true;
        }
        
    }
}
