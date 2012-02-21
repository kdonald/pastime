package com.pastime.next;

import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NextController {

    private final JdbcTemplate jdbcTemplate;
    
    public NextController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // http://pastimebrevard.com/hitmen/attendance/og-hitman?a=yes
    @RequestMapping(value="/{team}/attendance/{player}", method=RequestMethod.GET, params="a")
    public void markAttendance(@PathVariable String team, @PathVariable String player, @RequestParam boolean a) {
        jdbcTemplate.update("UPDATE next.game_attendance SET attending = ?, update_time = ? WHERE team = ? and player = ?", a, new Date(), team, player);
    }

    // http://pastimebrevard.com/hitmen/attendance/1/og-hitman?a=yes
    @RequestMapping(value="/{team}/attendance/{game}/{player}", method=RequestMethod.GET, params="a")
    public void markAttendance(@PathVariable String team, @PathVariable int game, @PathVariable String player, @RequestParam boolean a) {
        jdbcTemplate.update("UPDATE next.game_attendance SET attending = ?, update_time = ? WHERE team = ? and game = ? and player = ?", a, new Date(), team, game, player);        
    }
    
}
