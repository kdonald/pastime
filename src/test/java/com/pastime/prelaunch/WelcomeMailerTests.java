package com.pastime.prelaunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.mock_javamail.Mailbox;
import org.jvnet.mock_javamail.MockTransport;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.templating.JMustacheStringTemplateLoader;

public class WelcomeMailerTests {

    private WelcomeMailer welcomeMailer;

    @Before
    public void setUp() {
        MockJavaMailSender mailSender = new MockJavaMailSender();
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
        assertTrue(((String) message.getContent()).contains("Keith"));        
        assertTrue(((String) message.getContent()).contains("123456"));
    }
    
    private static class MockJavaMailSender extends JavaMailSenderImpl {

        private MockTransport transport;

        @Override
        protected Transport getTransport(Session session) throws NoSuchProviderException {
            this.transport = new MockTransport(session, null);
            return transport;
        }
    }

}
