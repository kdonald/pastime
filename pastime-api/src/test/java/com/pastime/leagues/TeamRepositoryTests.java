package com.pastime.leagues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.leagues.season.TeamForm;
import com.pastime.leagues.season.TeamRepository;

@ContextConfiguration(classes=TeamRepositoryTestsConfig.class)
@Transactional
@RunWith(value=SpringJUnit4ClassRunner.class)
public class TeamRepositoryTests {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Inject
    private TeamRepository teamRepository;

    @Test
    public void createTeam() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");                
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name) VALUES (1, 1, 'South Brevard Adult Flag Football')");
        TeamForm form = new TeamForm();
        form.setLeague(1);
        form.setSeason(1);
        form.setName("Hitmen");
        URI link = teamRepository.createTeam(form, 1);
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1"), link);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from teams where league = 1 and season = 1");
        assertEquals("Hitmen", record.get("name"));
        assertEquals("hitmen", record.get("slug"));        
        assertNull(record.get("franchise"));        
    }
    
    @Test
    public void createTeamFromFranchise() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO franchises (id, name, sport, founder) VALUES (1, 'Hitmen', 'Flag Football', 1)");
        jdbcTemplate.update("INSERT INTO franchise_members (franchise, player) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (1, 1, 'Admin')");        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name) VALUES (1, 1, 'South Brevard Adult Flag Football')");
        TeamForm form = new TeamForm();
        form.setLeague(1);
        form.setSeason(1);
        form.setName("Doesn't Matter");
        form.setFranchise(1);
        URI link = teamRepository.createTeam(form, 1);
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1"), link);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from teams where league = 1 and season = 1");
        assertEquals("Hitmen", record.get("name"));
        assertEquals("hitmen", record.get("slug"));        
        assertEquals(1, record.get("franchise"));
    }
    
}