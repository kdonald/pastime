package com.pastime.prelaunch.referrals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ReferralsController {

    private final ReferralProgram referralProgram;
    
    public ReferralsController(ReferralProgram referralProgram) {
        this.referralProgram = referralProgram;
    }

    @RequestMapping(value="/admin/referrals", method=RequestMethod.GET)
    public String referralsSummary(Model model) {
        model.addAttribute("totalReferrals", referralProgram.getTotalReferrals());
        return "prelaunch/referrals/all-summary";
    }

    @RequestMapping(value="/admin/referrals/detail", method=RequestMethod.GET)
    public String referralsDetail(Model model) {
        model.addAttribute("referrals", referralProgram.getAllReferrals());
        return "prelaunch/referrals/all-detail";
    }

    @RequestMapping(value="/referrals/{referralCode}", method=RequestMethod.GET)
    public String referralCode(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        Integer total = referralProgram.getTotalReferrals(referralCode);
        if (total == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("totalReferrals", total);     
        return "prelaunch/referrals/code-summary";
    }

    @RequestMapping(value="/referrals/{referralCode}/detail", method=RequestMethod.GET)
    public String referralCodeDetail(@PathVariable String referralCode, Model model) {
        model.addAttribute("referrals", referralProgram.getReferred(referralCode));        
        return "prelaunch/referrals/code-detail";
    }

}