package com.pastime.leagues.season;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Player is already playing")
public class AlreadyPlayingException extends RuntimeException {

    private TeamKey team;
    
    private Integer playerId;
    
    public AlreadyPlayingException(Integer id, TeamKey key) {
        this.playerId = id;
        this.team = key;
    }

    public TeamKey getTeam() {
        return team;
    }

    public Integer getPlayerId() {
        return playerId;
    }
    
}
