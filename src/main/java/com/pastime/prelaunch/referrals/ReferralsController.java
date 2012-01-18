package com.pastime.prelaunch.referrals;

import java.io.IOException;
import java.util.LinkedList;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.pastime.prelaunch.referrals.InsightRepository.ReferralInsights;

@Controller
public class ReferralsController {

    private final InsightRepository repository;
    
    @Inject
    public ReferralsController(InsightRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value="/referrals", method=RequestMethod.GET)
    public String referralsSummary(Model model) {
        model.addAttribute("totalReferrals", repository.getReferralInsights().getTotal());
        return "prelaunch/referrals/all-summary";
    }

    @RequestMapping(value="/referrals/detail", method=RequestMethod.GET)
    public String referralsDetail(Model model) {
        LinkedList<Referral> referrals = new LinkedList<Referral>();
        referrals.add(new Referral("January 18th", "Alexander W.", "Corgan D."));
        referrals.add(new Referral("January 18th", "Annabelle D.", "Keri D."));
        referrals.add(new Referral("January 18th", "Corgan D.", "Keri. D."));
        referrals.add(new Referral("January 17th", "Scot D.", "Keith D."));
        referrals.add(new Referral("January 17th", "Keri D.", "Keith D."));
        model.addAttribute("referrals", referrals);
        return "prelaunch/referrals/all-detail";
    }

    @RequestMapping(value="/referrals/{referralCode}", method=RequestMethod.GET)
    public String referralCode(@PathVariable String referralCode, Model model, HttpServletResponse response) throws IOException {
        ReferralInsights insights = repository.getReferralInsights(referralCode);
        if (insights == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("referralCode", referralCode);
        model.addAttribute("totalReferrals", insights.getTotal());     
        return "prelaunch/referrals/code-summary";
    }

}