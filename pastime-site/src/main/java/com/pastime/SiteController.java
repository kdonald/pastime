package com.pastime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SiteController {

    @RequestMapping(value="/", method=RequestMethod.GET, produces="text/html")
    public String home(Model model) {
        return "home/home";
    }
    
    // TODO custom request mapping condition required to confirm {organization} is actually a username that identifies an organization
    // See Skype chat history with Rossen
    @RequestMapping(value="/{organization}/{league}/{season}", method=RequestMethod.GET, produces="text/html")
    public String join(@PathVariable String organization, @PathVariable String league, @PathVariable Integer season, Model model) {
        return "leagues/join";
    }

}
