package com.pastime.players;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.util.Principal;

@ContextConfiguration(classes=PlayerRepositoryTestsConfig.class)
@Transactional
@RunWith(value=SpringJUnit4ClassRunner.class)
public class PlayerRepositoryTests {

    @Inject
    private JdbcTemplate jdbcTemplate;
    
    @Inject
    private PlayerRepository playerRepository;

    @Test
    public void findMe() throws Exception {
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");        
        Player me = playerRepository.findMe(new Principal(1));
        assertEquals((Integer) 1, me.getId());
        assertEquals(Gender.MALE, me.getGender());
        assertEquals(new URI("https://api.pastime.com/players/1/franchises"), me.getLinks().get("franchises"));
        assertEquals(new URI("https://api.pastime.com/players/1/picture"), me.getLinks().get("picture"));        
    }
    
    @Test
    public void findPlayerPicture() throws Exception {
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (2, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");                
        URI link = playerRepository.findPicture(2, PictureType.SMALL);
        assertNotNull(link);
        assertTrue(link.toString().endsWith("/players/2/small.png"));
    }
    
    @Test
    public void findDefaultPlayerPicture() throws Exception {
        jdbcTemplate.update("INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, password, referral_code) VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 'password', '123456')");
        URI link = playerRepository.findPicture(1, PictureType.SMALL);
        assertNotNull(link);
        assertTrue(link.toString().endsWith("/players/defaults/male/small.png"));
    }
    
}