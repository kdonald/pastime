package com.pastime.prelaunch;

import javax.validation.Valid;

import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PrelaunchController {

    private StringKeyGenerator referralCodeGenerator = new ReferralCodeGenerator();
    
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String comingSoon() {
        return "index.html";
    }
    
    @RequestMapping(value="/", method=RequestMethod.POST)
    public @ResponseBody Subscription subscribe(@Valid SubscribeForm form) {
        Subscription subscription = findSubscription(form.getEmail());
        if (subscription != null) {
            return subscription;
        }
        String referralCode = referralCodeGenerator.generateKey();
        Name name = Name.parseName(form.getName());
        subscription = createSubscription(name, referralCode);
        return subscription;
    }

    private Subscription findSubscription(String email) {
        return new Subscription("Keith", "http://pastimebrevard.com?ref=12345");
    }    

    private Subscription createSubscription(Name name, String referralCode) {
        return null;
    }


}
