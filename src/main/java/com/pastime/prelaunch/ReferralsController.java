package com.pastime.prelaunch;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ReferralsController {

    private final InsightRepository repository;
    
    @Inject
    public ReferralsController(InsightRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value="/referrals/{referralCode}", method=RequestMethod.GET)
    public String referralInsights(@PathVariable String referralCode, Model model) {
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referralInsights", repository.getReferralInsights(referralCode));
        return "referrals";
    }

    @RequestMapping(value="/cards/{referralCode}", method=RequestMethod.GET)
    public String card(@PathVariable String referralCode, Model model) {
        model.addAttribute("referralCode", referralCode);
        return "card";
    }
    
}