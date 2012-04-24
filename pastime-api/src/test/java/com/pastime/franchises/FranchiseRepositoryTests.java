package com.pastime.franchises;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(classes=FranchiseRepositoryTestsConfig.class)
@Transactional
@RunWith(value=SpringJUnit4ClassRunner.class)
public class FranchiseRepositoryTests {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Inject
    private FranchiseRepository franchiseRepository;

    @Test
    public void findFranchise() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");                
        jdbcTemplate.update("INSERT INTO franchises (id, name, sport, founder) VALUES (1, 'Hitmen', 'Flag Football', 1)");
        Franchise franchise = franchiseRepository.findFranchise(1);
        assertEquals("Hitmen", franchise.getName());
        assertEquals("Flag Football", franchise.getSport());
        assertEquals(new LocalDate(), franchise.getJoined());
        assertNull(franchise.getFounded());
        assertEquals(new URI("https://api.pastime.com/franchises/1/founder"), franchise.getLinks().get("founder"));
        assertEquals(new URI("https://api.pastime.com/franchises/1/picture"), franchise.getLinks().get("picture"));
        assertEquals(new URI("https://api.pastime.com/franchises/1/members"), franchise.getLinks().get("members"));        
    }
    
    @Test
    public void findQualifyingFranchises() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')"); 
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");                
        jdbcTemplate.update("INSERT INTO franchises (id, name, sport, founder) VALUES (1, 'Hitmen', 'Flag Football', 1)");
        jdbcTemplate.update("INSERT INTO franchise_members (franchise, player) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (1, 1, 'a')");
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (1, 'Brevard County Parks & Recreation')");
        jdbcTemplate.update("INSERT INTO leagues (organization, id, name, slug, sport, format, roster_min, roster_healthy, roster_max) VALUES (1, 1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7-on-7', 7, 10, 21)");
        List<Franchise> franchises = franchiseRepository.findQualifyingFranchises(1, 1);
        assertEquals(1, franchises.size());
        assertEquals("Hitmen", franchises.get(0).getName());
        assertEquals(new URI("https://api.pastime.com/franchises/1"), franchises.get(0).getUrl());        
    }
    
    @Test
    public void findCurrentFranchisePlayers() throws Exception {
        jdbcTemplate.update("INSERT INTO sports (name) VALUES ('Flag Football')");
        jdbcTemplate.update("INSERT INTO formats (name, sport) VALUES ('7-on-7', 'Flag Football')");
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");                
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (2, 'Alexander', 'Weaver', 'm', '1978-05-20', '32904', 'password', '234567')");                        
        jdbcTemplate.update("INSERT INTO franchises (id, name, sport, founder) VALUES (1, 'Hitmen', 'Flag Football', 1)");
        jdbcTemplate.update("INSERT INTO franchise_members (franchise, player) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO franchise_members (franchise, player, number, nickname, slug) VALUES (1, 2, 37, 'OG Hitman', 'og-hitman')");        
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (1, 1, 'a')");
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (1, 1, 'p')");
        jdbcTemplate.update("INSERT INTO franchise_member_roles (franchise, player, role) VALUES (1, 2, 'p')");
        jdbcTemplate.update("INSERT INTO usernames (username, franchise) VALUES ('hitmen', 1)");
        jdbcTemplate.update("INSERT INTO usernames (username, player) VALUES ('lexvan', 2)");        
        List<FranchiseMember> members = franchiseRepository.findFranchiseMembers(1, MemberRole.PLAYER, MemberStatus.CURRENT);
        assertEquals(2, members.size());
        FranchiseMember keith = members.get(0);
        assertEquals(null, keith.getNumber());
        assertEquals(null, keith.getNickname());
        assertEquals(new LocalDate(), keith.getJoined());
        assertEquals(new LocalDate(), keith.getRoles().get("Player").getBecame());
        assertEquals(new URI("https://api.pastime.com/franchises/1/members/1"), keith.getUrl());
        assertEquals(new URI("http://pastime.com/hitmen/1"), keith.getLinks().get("site"));
        FranchiseMember alex = members.get(1);
        assertEquals((Integer) 37, alex.getNumber());
        assertEquals("OG Hitman", alex.getNickname());
        assertEquals(new LocalDate(), alex.getJoined());
        assertEquals(new LocalDate(), alex.getRoles().get("Player").getBecame());        
        assertEquals(new URI("https://api.pastime.com/franchises/1/members/2"), alex.getUrl());
        assertEquals(new URI("http://pastime.com/hitmen/og-hitman"), alex.getLinks().get("site"));        
    }
    
}