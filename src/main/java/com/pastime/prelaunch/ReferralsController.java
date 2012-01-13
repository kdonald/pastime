package com.pastime.prelaunch;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.pastime.prelaunch.InsightRepository.ReferralInsights;

@Controller
public class ReferralsController {

    private final InsightRepository repository;
    
    @Inject
    public ReferralsController(InsightRepository repository) {
        this.repository = repository;
    }


    @RequestMapping(value="/referrals", method=RequestMethod.GET)
    public String referrals(Model model) {
        model.addAttribute("referralInsights", repository.getReferralInsights());
        return "referrals";
    }
    
    @RequestMapping(value="/referrals/{referralCode}", method=RequestMethod.GET)
    public String referralCode(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        ReferralInsights insights = repository.getReferralInsights(referralCode);
        if (insights == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("referralInsights", insights);     
        return "referralCode";
    }

    @RequestMapping(value="/cards/{referralCode}", method=RequestMethod.GET)
    public String card(@PathVariable String referralCode, Model model) {
        model.addAttribute("referralCode", referralCode);
        return "card";
    }
    
}