package com.pastime.next;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class NextController {

    private final JdbcTemplate jdbcTemplate;
    
    public NextController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // http://pastimebrevard.com/hitmen/attendance/og-hitman?a=yes
    @RequestMapping(value="/{team}/attendance/{player}", method=RequestMethod.GET, params="a")
    public void markAttendanceForGames(@Valid AttendanceUpdate update) {
        applyUpdate(update);
    }

    // http://pastimebrevard.com/hitmen/attendance/1/og-hitman?a=yes
    @RequestMapping(value="/{team}/attendance/{game}/{player}", method=RequestMethod.GET, params="a")
    public void markAttendanceForGame(@Valid AttendanceUpdate update) {
        applyUpdate(update);
    }
    
    private void applyUpdate(AttendanceUpdate update) {
        Date now = new Date();
        if (update.getGame() != null) {
            jdbcTemplate.update("UPDATE next.game_attendance SET attending = ?, update_time = ? WHERE team_slug = ? and game = ? and registered_player_slug = ?", update.getA(), now, update.getTeam(), update.getGame(), update.getPlayer());
        } else {
            jdbcTemplate.update("UPDATE next.game_attendance SET attending = ?, update_time = ? WHERE team_slug = ? and registered_player_slug = ?", update.getA(), now, update.getTeam(), update.getPlayer());            
        }
    }
    
}
