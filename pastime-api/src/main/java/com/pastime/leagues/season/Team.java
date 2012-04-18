package com.pastime.leagues.season;

import java.net.URI;

import com.pastime.leagues.season.AddPlayerForm.EmailAddress;

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

    public URI addPlayer(Integer userId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public URI addPlayer(EmailAddress email) {
        // TODO Auto-generated method stub
        return null;
    }
    
}