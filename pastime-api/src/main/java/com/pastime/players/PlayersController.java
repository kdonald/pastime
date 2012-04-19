package com.pastime.players;

import java.net.URI;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.util.Authorized;
import com.pastime.util.Principal;

@Controller
public class PlayersController {

    private final PlayerRepository playerRepository;

    @Inject
    public PlayersController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @RequestMapping(value="/me", method=RequestMethod.GET, produces="application/json")
    @Authorized
    public @ResponseBody Player me(Principal principal) {
        return playerRepository.findMe(principal);
    }
    
    @RequestMapping(value="/players/{id}/picture", method=RequestMethod.GET)
    public String userPicture(@PathVariable("id") Integer id, @RequestParam(value="type", defaultValue="SMALL") PictureType type) {
        URI imageLink = playerRepository.findPicture(id, type); 
        return "redirect:" + imageLink;      
    }

}