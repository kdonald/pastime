package com.pastime.franchises;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.util.Authorized;

@Controller
public class FranchisesController {

    private final FranchiseRepository franchiseRepository;

    @Inject
    public FranchisesController(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }
    
    @RequestMapping(value="/players/{player}/franchises",
            method=RequestMethod.GET, params="league", produces="application/json")
    @Authorized("franchises")
    public @ResponseBody List<Franchise> qualifyingFranchises(@RequestParam("league") Integer league, 
             @PathVariable("player") Integer player) {
        return franchiseRepository.findQualifyingFranchises(league, player);
    }

}