package com.pastime.leagues.season;

import com.pastime.util.ErrorReporter;
import com.pastime.util.Range;

public class Roster {
    
    private final Integer totalPlayerCount;
    
    private final Integer femalePlayerCount;

    private final Integer maxPlayers;

    private final Range age;
    
    private final TeamGender gender;

    private final Integer minFemales;
    
    public Roster(Integer totalPlayerCount, Integer femalePlayerCount, Integer maxPlayers, Range age, TeamGender gender, Integer minFemales) {
        this.totalPlayerCount = totalPlayerCount;
        this.femalePlayerCount = femalePlayerCount;
        this.maxPlayers = maxPlayers;        
        this.age = age;        
        this.gender = gender;
        this.minFemales = minFemales;        
    }

    public boolean isAcceptable(ProposedPlayer player, ErrorReporter reporter) {
        if (!hasRoomForMorePlayers()) {
            reporter.setMessage("max roster size has been reached");
            return false;
        }
        if (age != null && !age.inRange(player.getAge())) {
            reporter.setMessage("player age not in range required by this team");            
            return false;
        }        
        if (gender == TeamGender.MALE_ONLY && player.isFemale()) {
            reporter.setMessage("only male players are allowed on this team");
            return false;
        } else if (gender == TeamGender.FEMALE_ONLY && player.isMale()) {
            reporter.setMessage("only female players are allowed on this team");            
            return false;
        } else if (gender == TeamGender.CO_ED) {
            if (maxPlayers != null) {
                if (player.isMale() && spotsLeft() <= femalesStillNeeded()) {
                    reporter.setMessage("all roster spots left are reserved for females");                
                    return false;
                }                
            }
        }
        return true;
    }

    public boolean hasRoomForMorePlayers() {
        if (maxPlayers == null) {
            return true;
        }
        return totalPlayerCount < maxPlayers;
    }

    public Integer spotsLeft() {
        return maxPlayers - totalPlayerCount;
    }
    
    public Integer femalesStillNeeded() {
        if (minFemales == null) {
            return 0;
        }
        return Math.max(0, minFemales - femalePlayerCount);
    }
        
}