package com.pastime.old.next;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.SlugUtils;

public class AttendanceUpdate {

    @NotEmpty
    private String team;

    @NotEmpty
    private String player;
    
    private Integer game;
    
    @NotNull
    private Boolean a;

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = SlugUtils.toSlug(team);
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = SlugUtils.toSlug(player);
    }

    public Integer getGame() {
        return game;
    }

    public void setGame(Integer game) {
        this.game = game;
    }

    public Boolean getA() {
        return a;
    }

    public void setA(Boolean a) {
        this.a = a;
    }
       
}