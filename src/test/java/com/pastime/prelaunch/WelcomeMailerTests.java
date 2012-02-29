package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage.RecipientType;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.templating.JMustacheStringTemplateLoader;

import com.pastime.players.Name;

public class WelcomeMailerTests {

    private WelcomeMailer welcomeMailer;

    @Before
    public void setUp() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        JMustacheStringTemplateLoader templateLoader = new JMustacheStringTemplateLoader(new FileSystemResourceLoader());
        templateLoader.setPrefix("src/main/webapp/WEB-INF/views/");
        welcomeMailer = new WelcomeMailer(mailSender, templateLoader);
    }
    
    @Test
    public void sendWelcomeMail() throws Exception {
        Subscriber subscriber = new Subscriber("keith.donald@gmail.com", new Name("Keith", "Donald"), "123456", null, new Date(1327417603859L));
        welcomeMailer.subscriberAdded(subscriber);
        assertEquals(1, Mailbox.get("keith.donald@gmail.com").getNewMessageCount());
        Message message = Mailbox.get("keith.donald@gmail.com").get(0);
        assertEquals("Welcome to Pastime", message.getSubject());
        assertEquals("Keith Donald", ((InternetAddress) message.getFrom()[0]).getPersonal());        
        assertEquals("keith@pastimebrevard.com", ((InternetAddress) message.getFrom()[0]).getAddress());
        assertEquals("Keith Donald", ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getPersonal());        
        assertEquals("keith.donald@gmail.com", ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getAddress());
        assertTrue(((String) message.getContent()).contains("Keith"));        
        assertTrue(((String) message.getContent()).contains("123456"));
        
        Subscriber subscriber2 = new Subscriber("keridonald@gmail.com", new Name("Keri", "Donald"), "234567", null, new Date(1327417603859L));
        welcomeMailer.subscriberAdded(subscriber2);
        assertEquals(1, Mailbox.get("keridonald@gmail.com").getNewMessageCount());
        Message message2 = Mailbox.get("keridonald@gmail.com").get(0);
        assertEquals("Keith Donald", ((InternetAddress) message2.getFrom()[0]).getPersonal());        
        assertEquals("keith@pastimebrevard.com", ((InternetAddress) message2.getFrom()[0]).getAddress());
        assertEquals("Welcome to Pastime", message.getSubject());
        assertEquals("Keri Donald", ((InternetAddress) message2.getRecipients(RecipientType.TO)[0]).getPersonal());        
        assertEquals("keridonald@gmail.com", ((InternetAddress) message2.getRecipients(RecipientType.TO)[0]).getAddress());
        assertTrue(((String) message2.getContent()).contains("Keri"));        
        assertTrue(((String) message2.getContent()).contains("234567"));
    }

}
