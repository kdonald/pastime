package com.pastime.leagues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage.RecipientType;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.leagues.season.AddMemberForm;
import com.pastime.leagues.season.AddMemberResult;
import com.pastime.leagues.season.AddMemberResult.ResultType;
import com.pastime.leagues.season.AlreadyPlayingException;
import com.pastime.leagues.season.CreateTeamForm;
import com.pastime.leagues.season.InviteAnswer;
import com.pastime.leagues.season.SeasonKey;
import com.pastime.leagues.season.Team;
import com.pastime.leagues.season.TeamKey;
import com.pastime.leagues.season.TeamMember;
import com.pastime.leagues.season.TeamMemberInvite;
import com.pastime.leagues.season.TeamMemberRole;
import com.pastime.leagues.season.TeamRepository;
import com.pastime.players.Player;

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
        CreateTeamForm form = new CreateTeamForm();
        form.setName("Hitmen");
        URI link = teamRepository.createTeam(new SeasonKey(1, 1), form, 1);
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
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (1, 1, 'a')");        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        CreateTeamForm form = new CreateTeamForm();
        form.setName("Doesn't Matter");
        form.setFranchise(1);
        URI link = teamRepository.createTeam(new SeasonKey(1, 1), form, 1);
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1"), link);
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from teams where league = 1 and season = 1");
        assertEquals("Hitmen", record.get("name"));
        assertEquals("hitmen", record.get("slug"));        
        assertEquals(1, record.get("franchise"));
    }
    
    @Test
    public void findTeam() throws Exception {
        createTeam();
        Team team = teamRepository.findTeam(new TeamKey(1, 1, 1));
        assertEquals("Hitmen", team.getName());
    }
    
    @Test
    public void findTeamMember() throws Exception {
        addPlayerMe();
        TeamMember member = teamRepository.findMember(new TeamKey(1, 1, 1), 1);
        assertEquals("Keith Donald", member.getName());
    }
    
    @Test
    public void findTeamMemberInvite() throws Exception {
        addPlayerByEmail();
        TeamMemberInvite invite = teamRepository.findInvite(new TeamKey(1, 1, 1), "123456");
        assertEquals("Alexander Weaver", invite.getName());
        assertEquals("123456", invite.getCode());
        assertEquals(TeamMemberRole.PLAYER, invite.getRole());
        assertEquals(new URI("http://pastime.com/players/2"), invite.getLinks().get("site"));
    }
    
    @After
    public void clearMailbox() {
        Mailbox.clearAll();        
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
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'a')");
        AddMemberForm form = new AddMemberForm();
        AddMemberResult result = teamRepository.addMember(form, new TeamKey(1, 1, 1), 1);
        assertEquals(ResultType.MEMBER_CONFIRMED, result.getType());
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/members/1?role=PLAYER"), result.getLink());
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from team_member_roles where league = 1 and season = 1 and team = 1 and role = 'p'");
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
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'a')");
        AddMemberForm form = new AddMemberForm();
        form.setEmail("keith.donald@gmail.com");
        form.setName("Keith Donald");
        AddMemberResult result = teamRepository.addMember(form, new TeamKey(1, 1, 1), 1);        
        assertEquals(ResultType.MEMBER_CONFIRMED, result.getType());        
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/members/1?role=PLAYER"), result.getLink());
        Map<String, Object> record = jdbcTemplate.queryForMap("select * from team_member_roles where league = 1 and season = 1 and team = 1 and role = 'p'");
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
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'a')");
        teamRepository.setInviteGenerator(new StringKeyGenerator() {
            public String generateKey() {
                return "123456";
            }
        });
        AddMemberForm form = new AddMemberForm();
        form.setId(2);
        AddMemberResult result = teamRepository.addMember(form, new TeamKey(1, 1, 1), 1);                
        assertEquals(ResultType.MEMBER_INVITED, result.getType());        
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/invites/123456"), result.getLink());
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
        Mailbox.clearAll();        
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
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'a')");
        teamRepository.setInviteGenerator(new StringKeyGenerator() {
            public String generateKey() {
                return "123456";
            }
        });
        AddMemberForm form = new AddMemberForm();
        form.setEmail("alexander.weaver@gmail.com");
        form.setName("Alexander Weaver");
        AddMemberResult result = teamRepository.addMember(form, new TeamKey(1, 1, 1), 1);                
        assertEquals(ResultType.MEMBER_INVITED, result.getType());        
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/invites/123456"), result.getLink());
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
    public void addPersonByEmail() throws Exception {
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
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'a')");
        teamRepository.setInviteGenerator(new StringKeyGenerator() {
            public String generateKey() {
                return "123456";
            }
        });        
        AddMemberForm form = new AddMemberForm();
        form.setEmail("alexander.weaver@gmail.com");
        form.setName("Alexander Weaver");
        AddMemberResult result = teamRepository.addMember(form, new TeamKey(1, 1, 1), 1);                
        assertEquals(ResultType.MEMBER_INVITED, result.getType());        
        assertEquals(new URI("https://api.pastime.com/leagues/1/seasons/1/teams/1/invites/123456"), result.getLink());
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
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (2, 'Alexander', 'Weaver', 'm', '1978-05-20', '32904', 'password', '234567')");
        jdbcTemplate.update("INSERT INTO player_emails (player, email, primary_email) VALUES (2, 'alexander.weaver@gmail.com', true)");        
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        jdbcTemplate.update("INSERT INTO seasons (league, number, name, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', 'o')");
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug) VALUES (1, 1, 1, 'Hitmen', 'hitmen')");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 1)");
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player) VALUES (1, 1, 1, 2)");        
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 1, 'a')");
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 2, 'p')");        
        AddMemberForm form = new AddMemberForm();
        form.setId(2);
        teamRepository.addMember(form, new TeamKey(1, 1, 1), 1);                
    }

    @Test
    public void answerInvite() throws Exception {
        addPlayerByEmail();
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE code = '123456' and accepted = false)", Boolean.class));        
        teamRepository.answerInvite(new TeamKey(1, 1, 1), "123456", InviteAnswer.ACCEPT, 2);
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE code = '123456' and accepted = true)", Boolean.class));                
    }

    @Test(expected=IllegalArgumentException.class)
    public void answerInviteNotIntendedForYou() throws Exception {
        addPlayerByEmail();
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE code = '123456' and accepted = false)", Boolean.class));        
        teamRepository.answerInvite(new TeamKey(1, 1, 1), "123456", InviteAnswer.ACCEPT, 1);
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE code = '123456' and accepted = true)", Boolean.class));                
    }

    @Test
    public void removeMember() throws Exception {
        addPlayerMe();        
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles r WHERE league = 1 AND season = 1 AND team = 1 AND player = 1 AND role = 'p')", Boolean.class));        
        teamRepository.removeMemberRole(new TeamKey(1, 1, 1), 1, TeamMemberRole.PLAYER, 1);
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members r WHERE league = 1 AND season = 1 AND team = 1 AND player = 1)", Boolean.class));                
        assertFalse(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles r WHERE league = 1 AND season = 1 AND team = 1 AND player = 1 AND role = 'p')", Boolean.class));
        teamRepository.removeMemberRole(new TeamKey(1, 1, 1), 1, TeamMemberRole.ADMIN, 1); 
        assertFalse(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members r WHERE league = 1 AND season = 1 AND team = 1 AND player = 1)", Boolean.class));        
    }
    
    @Test
    public void cancelInvite() throws Exception {
        addPlayerByEmail();        
        assertTrue(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE code = '123456')", Boolean.class));        
        teamRepository.cancelInvite(new TeamKey(1, 1, 1), "123456", 1);
        assertFalse(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_invites WHERE code = '123456')", Boolean.class));        
    }
    
}