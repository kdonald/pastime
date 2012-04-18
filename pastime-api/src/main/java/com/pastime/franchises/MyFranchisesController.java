package com.pastime.franchises;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.util.Authorized;
import com.pastime.util.Principal;

@Controller
public class MyFranchisesController {

    private final FranchiseRepository franchiseRepository;

    @Inject
    public MyFranchisesController(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }
    
    @RequestMapping(value="/me/franchises", method=RequestMethod.GET, params="league", produces="application/json")
    @Authorized("franchises")
    public @ResponseBody List<Franchise> qualifyingFranchises(@RequestParam("league") Integer league, Principal principal) {
        return franchiseRepository.findQualifyingFranchises(league, principal.getPlayerId());
    }

}