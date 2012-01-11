package com.pastime.prelaunch;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PrelaunchController {

    @RequestMapping(value="/", method=RequestMethod.GET)
    public String comingSoon() {
        return "index.html";
    }
    
    @RequestMapping(value="/", method=RequestMethod.POST)
    public @ResponseBody Subscription subscribe(@Valid SubscribeForm form) {
        return new Subscription(form.getName(), "http://pastimebrevard.com?ref=12345");
    }
    
}
