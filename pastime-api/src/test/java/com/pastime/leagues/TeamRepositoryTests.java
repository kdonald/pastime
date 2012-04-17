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

import com.pastime.leagues.season.Player;
import com.pastime.leagues.season.Team;
import com.pastime.leagues.season.TeamKey;
import com.pastime.leagues.season.TeamRepository;
import com.pastime.util.ErrorReporter;
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
        Team team = teamRepository.findTeam(new TeamKey(1, 1, 1));
        Player player = new Player(Gender.MALE, new LocalDate(1977, 12, 29));
        ErrorReporter reporter = new ErrorReporter();
        assertEquals((Integer) 19, team.getRoster().spotsLeft());
        assertTrue(team.getRoster().isAcceptable(player, reporter));
    }
}