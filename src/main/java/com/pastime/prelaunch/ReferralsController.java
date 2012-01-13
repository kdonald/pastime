package com.pastime.prelaunch;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ReferralsController {

    private ReferralAnalytics analytics;
    
    public ReferralsController() {
        analytics = new ReferralAnalytics();
    }

    @RequestMapping(value="/referrals/{referralCode}", method=RequestMethod.GET)
    public String referralInsights(@PathVariable String referralCode, Model model) {
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referralAnalytics", analytics);
        return "referrals";
    }

    @RequestMapping(value="/cards/{referralCode}", method=RequestMethod.GET)
    public String card(@PathVariable String referralCode, Model model) {
        model.addAttribute("referralCode", referralCode);
        return "card";
    }

    public static class ReferralAnalytics {
        public int getTotalReferralCount() {
            return 48;
        }
    }
    
}