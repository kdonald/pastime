package com.pastime.prelaunch.referrals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.pastime.prelaunch.SubscriptionRepository;
import com.pastime.util.DefaultReferralCodeGenerator;
import com.pastime.util.ReferralCodeGenerator;

@Controller
@RequestMapping("/referrals")
public class ReferralsController {

    private final ReferralProgram referralProgram;
    
    private final ReferralCodeGenerator codeGenerator = new DefaultReferralCodeGenerator();
    
    private final SubscriptionRepository subscriptionRepository;
    
    public ReferralsController(ReferralProgram referralProgram, SubscriptionRepository subscriptionRepository) {
        this.referralProgram = referralProgram;
        this.subscriptionRepository = subscriptionRepository;
    }

    @RequestMapping(value="/{referralCode}", method=RequestMethod.GET)
    public String summary(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        referralCode = cleanse(referralCode);
        String name = exists(referralCode, response);
        if (name == null) {
            return null;
        }
        model.addAttribute("name", name);        
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("totalReferrals", referralProgram.getTotalReferrals(referralCode));  
        return "prelaunch/referrals/code-summary";
    }

    @RequestMapping(value="/{referralCode}/detail", method=RequestMethod.GET)
    public String detail(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        referralCode = cleanse(referralCode);
        String name = exists(referralCode, response);
        if (name == null) {
            return null;
        }
        model.addAttribute("name", name);        
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referrals", referralProgram.getReferred(referralCode));        
        return "prelaunch/referrals/code-detail";
    }

    @RequestMapping(value="/{referralCode}/more", method=RequestMethod.GET)
    public String more(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        referralCode = cleanse(referralCode);
        String name = exists(referralCode, response);
        if (name == null) {
            return null;
        }
        model.addAttribute("name", name);        
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referralLink", "http://pastime.com/?r=" + referralCode);
        return "prelaunch/referrals/code-more";
    }

    private String cleanse(String referralCode) {
        referralCode = referralCode.toLowerCase();
        if (!codeGenerator.meetsSyntax(referralCode)) {
            throw new IllegalArgumentException("Not valid referral code syntax");            
        }
        return referralCode;
    }
    
    private String exists(String referralCode, HttpServletResponse response) throws IOException {
        String name = subscriptionRepository.findFirstName(referralCode);
        if (name == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        return name;
    }
    
}