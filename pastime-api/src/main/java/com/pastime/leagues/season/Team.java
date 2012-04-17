package com.pastime.leagues.season;

import java.net.URI;

import com.pastime.leagues.season.AddPlayerForm.EmailAddress;
import com.pastime.util.Principal;

public class Team {

    private TeamKey key;
    
    private Roster roster;

    public Team(TeamKey key, Roster roster) {
        this.key = key;
        this.roster = roster;
    }

    public TeamKey getKey() {
        return key;
    }
    
    public Roster getRoster() {
        return roster;
    }

    public URI addPlayer(Integer userId, Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public URI addPlayer(EmailAddress email, Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }
    
}