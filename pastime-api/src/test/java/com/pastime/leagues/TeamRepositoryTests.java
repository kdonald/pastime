package com.pastime.leagues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage.RecipientType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.leagues.season.AddPlayerForm.EmailAddress;
import com.pastime.leagues.season.AlreadyPlayingException;
import com.pastime.leagues.season.Team;
import com.pastime.leagues.season.TeamForm;
import com.pastime.leagues.season.TeamKey;
import com.pastime.leagues.season.TeamRepository;
import com.pastime.players.Player;
import com.pastime.util.Name;

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
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
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
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
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
    
    @Test
    public void searchPlayers() {
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (2, 'Keith', 'Jones', 'm', '1979-04-29', '32904', 'password', '234567')");
        List<Player> players = teamRepository.searchPlayers(new TeamKey(1, 1, 1), "Keith", 1);
        assertEquals(1, players.size());
        assertEquals("Keith Jones", players.get(0).getName());
    }
    
    @Test
    public void addPlayerMe() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (1, 'keith.donald@gmail.com', true)");                        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug) VALUES (1, 1, 1, 'Hitmen', 'hitmen')");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 1)");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'Admin')");        
        Team team = teamRepository.getTeamForEditing(new TeamKey(1, 1, 1), 1);
        URI uri = team.addPlayer(1);
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/members/1"), uri);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from team_member_roles where league = 1 and season = 1 and team = 1 and role = 'Player'");
        assertEquals(1, record.get("player"));
    }
    
    @Test
    public void addPlayerMeByEmail() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (1, 'keith.donald@gmail.com', true)");                        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug) VALUES (1, 1, 1, 'Hitmen', 'hitmen')");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 1)");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'Admin')");        
        Team team = teamRepository.getTeamForEditing(new TeamKey(1, 1, 1), 1);
        URI uri = team.addPlayer(new EmailAddress("keith.donald@gmail.com", new Name("Keith" ,"Donald")));
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/members/1"), uri);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from team_member_roles where league = 1 and season = 1 and team = 1 and role = 'Player'");
        assertEquals(1, record.get("player"));
    }
    
    @Test
    public void addPlayer() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (1, 'keith.donald@gmail.com', true)");             
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (2, 'Alexander', 'Weaver', 'm', '1978-05-20', '32904', 'password', '234567')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (2, 'alexander.weaver@gmail.com', true)");        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug) VALUES (1, 1, 1, 'Hitmen', 'hitmen')");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 1)");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'Admin')");
        teamRepository.setInviteGenerator(new StringKeyGenerator() {
            public String generateKey() {
                return "123456";
            }
        });
        Team team = teamRepository.getTeamForEditing(new TeamKey(1, 1, 1), 1);
        URI uri = team.addPlayer(2);
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/invites/123456"), uri);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from team_member_invites where league = 1 and season = 1 and team = 1 and code = '123456'");
        assertEquals(2, record.get("player"));
        assertEquals(1, Mailbox.get("alexander.weaver@gmail.com").getNewMessageCount());
        Message message = Mailbox.get("alexander.weaver@gmail.com").get(0);
        assertEquals("Confirm your Hitmen team membership", message.getSubject());
        assertEquals("Pastime Invites", ((InternetAddress) message.getFrom()[0]).getPersonal());        
        assertEquals("invites@pastime.com", ((InternetAddress) message.getFrom()[0]).getAddress());
        assertEquals("Alexander Weaver", ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getPersonal());        
        assertEquals("alexander.weaver@gmail.com", ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getAddress());
        assertTrue(((String) message.getContent()).contains("Keith"));        
        assertTrue(((String) message.getContent()).contains("123456"));        
    }
    
    @Test
    public void addPlayerByEmail() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (1, 'keith.donald@gmail.com', true)");             
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (2, 'Alexander', 'Weaver', 'm', '1978-05-20', '32904', 'password', '234567')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (2, 'alexander.weaver@gmail.com', true)");        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug) VALUES (1, 1, 1, 'Hitmen', 'hitmen')");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 1)");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'Admin')");
        teamRepository.setInviteGenerator(new StringKeyGenerator() {
            public String generateKey() {
                return "123456";
            }
        });        
        Team team = teamRepository.getTeamForEditing(new TeamKey(1, 1, 1), 1);
        URI uri = team.addPlayer(new EmailAddress("alexander.weaver@gmail.com", new Name("Alexander" ,"Weaver")));
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/invites/123456"), uri);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from team_member_invites where league = 1 and season = 1 and team = 1 and code = '123456'");
        assertEquals(2, record.get("player"));
        assertEquals(1, Mailbox.get("alexander.weaver@gmail.com").getNewMessageCount());
        Message message = Mailbox.get("alexander.weaver@gmail.com").get(0);
        assertEquals("Confirm your Hitmen team membership", message.getSubject());
        assertEquals("Pastime Invites", ((InternetAddress) message.getFrom()[0]).getPersonal());        
        assertEquals("invites@pastime.com", ((InternetAddress) message.getFrom()[0]).getAddress());
        assertEquals("Alexander Weaver", ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getPersonal());        
        assertEquals("alexander.weaver@gmail.com", ((InternetAddress) message.getRecipients(RecipientType.TO)[0]).getAddress());
        assertTrue(((String) message.getContent()).contains("Keith"));        
        assertTrue(((String) message.getContent()).contains("123456"));        
    }
    
    @Test(expected=AlreadyPlayingException.class)
    public void addPlayerAlreadyPlaying() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (1, 'keith.donald@gmail.com', true)");                        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug) VALUES (1, 1, 1, 'Hitmen', 'hitmen')");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 1)");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'Admin')");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'Player')");        
        Team team = teamRepository.getTeamForEditing(new TeamKey(1, 1, 1), 1);
        team.addPlayer(1);
    }
    
}