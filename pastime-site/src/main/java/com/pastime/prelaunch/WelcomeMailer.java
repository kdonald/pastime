package com.pastime.prelaunch;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.templating.StringTemplateLoader;

public class WelcomeMailer implements SubscriberListener {

    private JavaMailSender mailSender;

    private StringTemplateLoader templateLoader;
    
    @Inject
    public WelcomeMailer(JavaMailSender mailSender, StringTemplateLoader templateLoader) {
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
    }
    
    @Async
    public void subscriberAdded(final Subscriber subscriber) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper welcome = new MimeMessageHelper(message);
               welcome.setFrom(new InternetAddress("keith@pastime.com", "Keith Donald"));
               welcome.setTo(new InternetAddress(subscriber.getEmail(), subscriber.getName().toString()));
               welcome.setSubject("Welcome to Pastime");
               Map<String, Object> model = new HashMap<String, Object>(2, 1);
               model.put("firstName", subscriber.getName().getFirstName());
               model.put("body", welcomeBody(subscriber));
               welcome.setText(templateLoader.getTemplate("mail/founder-letter").render(model), true);
            }
         };        
        mailSender.send(preparator);
    }

    private String welcomeBody(Subscriber subscriber) {
        Map<String, Object> model = new HashMap<String, Object>(2, 1); 
        model.put("referralLink", "http://pastime.com/?r=" + subscriber.getReferralCode());
        model.put("referralInsightsLink", "http://pastime.com/referrals/" + subscriber.getReferralCode());
        return templateLoader.getTemplate("prelaunch/mail/welcome").render(model);
    }
    
    //cglib ceremony
    public WelcomeMailer() {};

}