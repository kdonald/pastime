package com.pastime.next;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Controller;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.ui.Model;
import org.springframework.util.SlugUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NextController {

    private final JdbcTemplate jdbcTemplate;
    
    private final JavaMailSender mailSender;

    private StringTemplateLoader templateLoader;

    public NextController(JdbcTemplate jdbcTemplate, JavaMailSender mailSender, StringTemplateLoader templateLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
    }

    // http://pastime.com/hitmen/attendance/og-hitman?a=yes
    @RequestMapping(value="/{team}/attendance/{player}", method=RequestMethod.GET, params="a")
    public String markAttendanceForGames(@Valid AttendanceUpdate update, Model model) {
        return applyUpdate(update, model);
    }

    // http://pastime.com/hitmen/attendance/1/og-hitman?a=yes
    @RequestMapping(value="/{team}/attendance/{game}/{player}", method=RequestMethod.GET, params="a")
    public String markAttendanceForGame(@Valid AttendanceUpdate update, Model model) {
        return applyUpdate(update, model);
    }
    
    @RequestMapping(value="/admin/next", method=RequestMethod.POST)
    public @ResponseBody String processNextGames(@RequestParam String team) {
        final String teamSlug = SlugUtils.toSlug(team);
        final Map<String, Object> nextGame = jdbcTemplate.queryForMap("SELECT number, start_time, opponent, game FROM next.games WHERE team_slug = ? ORDER BY number", teamSlug);
        Long game = (Long) nextGame.get("game");
        final Map<String, Object> captain = jdbcTemplate.queryForMap("SELECT first_name, last_name, email FROM players WHERE id = (SELECT t.captain FROM games g INNER JOIN registered_teams t ON g.registered_team = t.id WHERE g.id = ?)", game);
        List<Map<String, Object>> players = jdbcTemplate.queryForList("SELECT a.registered_player_slug, a.name, p.first_name, p.last_name, p.email FROM next.game_attendance a INNER JOIN players p ON a.player = p.id WHERE a.team_slug = ? AND a.game = ?", teamSlug, nextGame.get("number"));
        for (final Map<String, Object> player : players) {
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage message) throws Exception {
                   MimeMessageHelper attendance = new MimeMessageHelper(message);
                   attendance.setFrom(new InternetAddress((String) captain.get("email"), captain.get("first_name") + " " + captain.get("last_name")));
                   attendance.setTo(new InternetAddress((String) player.get("email"), player.get("first_name") + " " + player.get("last_name")));
                   attendance.setSubject("You have a game today. Are you in?");
                   Map<String, Object> model = new HashMap<String, Object>(5, 1);
                   model.put("name", player.get("name"));
                   Date startTime = (Date) nextGame.get("start_time");
                   model.put("time", DateFormat.getTimeInstance(DateFormat.SHORT).format(startTime));
                   model.put("opponent", nextGame.get("opponent"));
                   model.put("team", teamSlug);
                   model.put("player", player.get("registered_player_slug"));
                   attendance.setText(templateLoader.getTemplate("next/mail/attendance-today").render(model), true);
                }
             };        
            mailSender.send(preparator);            
        }
        return "success";
    }
    
    private String applyUpdate(AttendanceUpdate update, Model model) {
        Date now = new Date();
        if (update.getGame() != null) {
            jdbcTemplate.update("UPDATE next.game_attendance SET attending = ?, update_time = ? WHERE team_slug = ? AND game = ? and registered_player_slug = ?", update.getA(), now, update.getTeam(), update.getGame(), update.getPlayer());
        } else {
            jdbcTemplate.update("UPDATE next.game_attendance SET attending = ?, update_time = ? WHERE team_slug = ? AND registered_player_slug = ?", update.getA(), now, update.getTeam(), update.getPlayer());            
        }
        model.addAttribute("attending", update.getA());
        model.addAttribute("name", jdbcTemplate.queryForObject("SELECT name FROM next.game_attendance where team_slug = ? AND game = 1 AND registered_player_slug = ?", String.class, update.getTeam(), update.getPlayer()));
        return "next/attendance-confirmation";        
    }
    
}
