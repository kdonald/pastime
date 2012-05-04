package com.pastime.home;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class SiteController {

    private String apiUrl = "http://localhost:8081";
    
    private JdbcTemplate jdbcTemplate;
    
    private String seasonByNumberSql;

    private String seasonBySlugSql;

    private String teamApiSql;
    
    public SiteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        seasonByNumberSql = SqlUtils.sql(new ClassPathResource("season-by-number.sql", getClass()));
        seasonBySlugSql = SqlUtils.sql(new ClassPathResource("season-by-slug.sql", getClass()));
        teamApiSql = SqlUtils.sql(new ClassPathResource("team-api.sql", getClass()));        
    }
    
    @RequestMapping(value="/", method=RequestMethod.GET, produces="text/html")
    public String home(Model model) {
        return "home/home";
    }
    
    // TODO custom request mapping condition required to confirm {organization} is actually a username that identifies an organization; see Skype chat history with Rossen
    @RequestMapping(value="/{organization}/{league}/{season}", method=RequestMethod.GET, produces="text/html")
    public String join(@PathVariable("organization") String organization, @PathVariable("league") String league, @PathVariable("season") String season,
            final Model model, HttpServletResponse response) throws IOException {
        organization = organization.trim();
        league = league.trim();
        season = season.trim();
        SeasonModelPopulator populator = new SeasonModelPopulator(model);
        Integer seasonNumber = seasonNumber(season);
        if (seasonNumber != null) {
            jdbcTemplate.query(seasonByNumberSql, populator, organization, league, seasonNumber);
        } else {
            jdbcTemplate.query(seasonBySlugSql, populator, organization, league, season);            
        }
        if (populator.getInvocationCount() == 0) {
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return null;
        }
        if (populator.getInvocationCount() > 1) {
            throw new IllegalStateException("Should not happen");
        }
        model.addAttribute("apiUrl", apiUrl);
        return "leagues/join";
    }

    @RequestMapping(value="/{organization}/{league}/{season}/{team}", method=RequestMethod.GET, params="invite", produces="text/html")
    public String answerInvite(@PathVariable("organization") String organization, @PathVariable("league") String league,
            @PathVariable("season") String season, @PathVariable("team") String team,
            @RequestParam("invite") String invite, @RequestParam(value="a", required=false) String indication, final Model model) {
        Map<String, Object> apiVars = jdbcTemplate.queryForMap(teamApiSql, organization, league, season, team);
        apiVars.put("invite", invite);
        model.addAttribute("inviteUrl", UriComponentsBuilder.fromHttpUrl(apiUrl).path("/leagues/{league}/seasons/{season}/teams/{team}/invites/{invite}").buildAndExpand(apiVars));
        if (indication != null) {
            if (indication.equals("a")) {
                model.addAttribute("indication", "ACCEPT");
            } else if (indication.equals("d")) {
                model.addAttribute("indication", "DECLINE");
            }
        }
        return "leagues/invite";
    }
    
    // internal helpers
    
    private Integer seasonNumber(String season) {
        try {
            return Integer.parseInt(season);
        } catch (NumberFormatException e) {
            return null;
        }        
    }
    
    private static class SeasonModelPopulator implements RowCallbackHandler {
        
        private final Model model;
        
        private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MM/dd/YYYY");

        private int invocationCount;
        
        public SeasonModelPopulator(Model model) {
            this.model = model;
        }

        public int getInvocationCount() {
            return invocationCount;
        }
        
        public void processRow(ResultSet rs) throws SQLException {
            model.addAttribute("name", rs.getString("name"));
            model.addAttribute("start_date", formatDate(rs.getDate("start_date")));
            model.addAttribute("roster_min", rs.getInt("roster_min"));
            model.addAttribute("roster_max", rs.getInt("roster_max"));
            model.addAttribute("league_id", rs.getInt("league_id"));
            model.addAttribute("number", rs.getInt("number"));
            invocationCount++;
        }
        
        private String formatDate(Date date) {
            if (date == null) {
                return "";
            }
            return dateFormatter.print(date.getTime());
        }
        
    }

}