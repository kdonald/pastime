package com.pastime.franchises;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.pastime.players.PictureType;

@Controller
@RequestMapping("/franchises/{id}")
public class FranchiseController {

    private final FranchiseRepository franchiseRepository;

    @Inject
    public FranchiseController(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = franchiseRepository;
    }

    @RequestMapping(method=RequestMethod.GET, produces="application/json")
    public Franchise franchise(@PathVariable("id") Integer id) {
        return franchiseRepository.findFranchise(id);
    }

    @RequestMapping(value="/founder", method=RequestMethod.GET, produces="application/json")
    public String founder(@PathVariable("id") Integer id) {
        return "redirect:" + franchiseRepository.findFounder(id);
    }
    
    @RequestMapping(value="/picture", method=RequestMethod.GET, produces="application/json")
    public String picture(@PathVariable("id") Integer id,
            @RequestParam(value="type", defaultValue="small") PictureType type) {
        return "redirect:" + franchiseRepository.findPicture(id, type);
    }

    @RequestMapping(value="/members", method=RequestMethod.GET, produces="application/json")
    public List<FranchiseMember> members(@PathVariable("id") Integer id,
            @RequestParam(value="role", defaultValue="player") MemberRole role,
            @RequestParam(value="status", defaultValue="current") MemberStatus status) {
        return franchiseRepository.findFranchiseMembers(id, role, status);
    }

    @RequestMapping(value="/members/{member}", method=RequestMethod.GET, produces="application/json")
    public FranchiseMember member(@PathVariable("id") Integer id, @PathVariable("member") Integer memberId) {
        return franchiseRepository.findFranchiseMember(id, memberId);        
    }
    
    @RequestMapping(value="/members/{member}/picture", method=RequestMethod.GET)
    public String memberPicture(@PathVariable("id") Integer id, @PathVariable("member") Integer memberId) {
        return "redirect:" + franchiseRepository.findFranchiseMemberPicture(id, memberId);
    }
    
}