package com.pastime.prelaunch;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PrelaunchController {

    private SubscriptionRepository subscriptionRepository;
    
    public PrelaunchController(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(@RequestParam(required=false) String r, Model model) {
        String firstName = subscriptionRepository.findFirstName(r);
        if (firstName != null) {
            model.addAttribute("referred", true);
            model.addAttribute("referredName", firstName);
            model.addAttribute("referralCode", r);
        }
        return "prelaunch/index";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody Subscription subscribe(@Valid SubscribeForm form) {
        return subscriptionRepository.subscribe(form);
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String privacy(Model model) {
        return "prelaunch/about";
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.GET)
    public String unsubscribeForm(Model model) {
        return "prelaunch/unsubscribe";
    }

    @RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
    public String unsubscribe(@Valid UnsubscribeForm form) {
        subscriptionRepository.unsubscribe(form.getEmail());
        return "redirect:/";
    }

}