package com.pastime.prelaunch.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.pastime.prelaunch.referrals.ReferralProgram;

@Controller
@RequestMapping("/admin/referrals")
public class ReferralsAdminController {

    private final ReferralProgram referralProgram;
    
    public ReferralsAdminController(ReferralProgram referralProgram) {
        this.referralProgram = referralProgram;
    }

    @RequestMapping(method=RequestMethod.GET)
    public String referralsSummary(Model model) {
        model.addAttribute("totalReferrals", referralProgram.getTotalReferrals());
        return "prelaunch/admin/referrals-summary";
    }

    @RequestMapping(value="/detail", method=RequestMethod.GET)
    public String referralsDetail(Model model) {
        model.addAttribute("referrals", referralProgram.getAllReferrals());
        return "prelaunch/admin/referrals-detail";
    }
    
}
