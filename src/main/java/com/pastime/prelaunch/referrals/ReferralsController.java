package com.pastime.prelaunch.referrals;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

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

    @RequestMapping(value="/referrals", method=RequestMethod.GET)
    public String referralsSummary(Model model) {
        model.addAttribute("totalReferrals", repository.getTotalReferrals());
        return "prelaunch/referrals/all-summary";
    }

    @RequestMapping(value="/referrals/detail", method=RequestMethod.GET)
    public String referralsDetail(Model model) {
        model.addAttribute("referrals", repository.getAllReferrals());
        return "prelaunch/referrals/all-detail";
    }

    @RequestMapping(value="/referrals/{referralCode}", method=RequestMethod.GET)
    public String referralCode(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        Integer total = repository.getTotalReferrals(referralCode);
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
        model.addAttribute("referrals", repository.getReferred(referralCode));        
        return "prelaunch/referrals/code-detail";
    }

}