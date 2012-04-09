package com.pastime.leagues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.util.Gender;

@ContextConfiguration(classes=TeamRepositoryTestsConfig.class)
@Transactional
@RunWith(value=SpringJUnit4ClassRunner.class)
public class TeamRepositoryTests {

    @Inject
    private TeamRepository teamRepository;

    @Test
    @Transactional
    public void findTeam() {
        Team team = teamRepository.findTeam(1, 1, 1);
        assertEquals((Integer) 1, team.getLeague());
        assertEquals((Integer) 1, team.getSeason());
        assertEquals((Integer) 1, team.getNumber());
        Player player = new Player(Gender.MALE, new LocalDate(1977, 12, 29));
        ErrorReporter reporter = new ErrorReporter();
        assertEquals((Integer) 19, team.getRoster().spotsLeft());
        assertTrue(team.getRoster().isAcceptable(player, reporter));
    }
}