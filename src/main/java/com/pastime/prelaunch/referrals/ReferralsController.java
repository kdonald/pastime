package com.pastime.prelaunch.referrals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.pastime.prelaunch.DefaultReferralCodeGenerator;
import com.pastime.prelaunch.ReferralCodeGenerator;

@Controller
@RequestMapping("/referrals")
public class ReferralsController {

    private final ReferralProgram referralProgram;
    
    private final ReferralCodeGenerator codeGenerator = new DefaultReferralCodeGenerator();
    
    public ReferralsController(ReferralProgram referralProgram) {
        this.referralProgram = referralProgram;
    }

    @RequestMapping(value="/{referralCode}", method=RequestMethod.GET)
    public String referralCode(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        referralCode = cleanse(referralCode);
        Integer total = referralProgram.getTotalReferrals(referralCode);
        if (total == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("totalReferrals", total);     
        return "prelaunch/referrals/code-summary";
    }

    @RequestMapping(value="/{referralCode}/detail", method=RequestMethod.GET)
    public String referralCodeDetail(@PathVariable String referralCode, Model model) {
        referralCode = cleanse(referralCode);        
        model.addAttribute("referrals", referralProgram.getReferred(referralCode));        
        return "prelaunch/referrals/code-detail";
    }

    private String cleanse(String referralCode) {
        referralCode = referralCode.toLowerCase();
        if (!codeGenerator.meetsSyntax(referralCode)) {
            throw new IllegalArgumentException("Not valid referral code syntax");            
        }
        return referralCode;
    }
    
}