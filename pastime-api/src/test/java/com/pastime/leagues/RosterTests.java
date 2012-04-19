package com.pastime.leagues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.pastime.leagues.season.ProposedPlayer;
import com.pastime.leagues.season.Roster;
import com.pastime.leagues.season.TeamGender;
import com.pastime.util.ErrorReporter;
import com.pastime.util.Gender;
import com.pastime.util.Name;
import com.pastime.util.Range;

public class RosterTests {

    ErrorReporter reporter = new ErrorReporter();

    @Test
    public void rosterMaxSize() {
        Roster roster = new Roster(0, 0, null, null, TeamGender.DOES_NOT_MATTER, null);
        ProposedPlayer player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, new LocalDate(1977, 12, 29));
        boolean result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());
        
        reporter.clear();
        
        roster = new Roster(9, 0, 10, null, TeamGender.DOES_NOT_MATTER, null);
        result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());
        
        reporter.clear();
        
        roster = new Roster(10, 0, 10, null, TeamGender.DOES_NOT_MATTER, null);
        result = roster.isAcceptable(player, reporter);
        assertFalse(result);
        assertNotNull(reporter.getMessage());
        
    }
    
    @Test
    public void rosterAge() {
        Roster roster = new Roster(0, 0, null, new Range(30, 50), TeamGender.DOES_NOT_MATTER, null);
        ProposedPlayer player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, new LocalDate(1977, 12, 29));
        boolean result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());

        reporter.clear();
        
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, new LocalDate().minusYears(29));
        result = roster.isAcceptable(player, reporter);
        assertFalse(result);
        assertNotNull(reporter.getMessage());

        reporter.clear();
        
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, new LocalDate().minusYears(50));
        result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());

        reporter.clear();
        
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, new LocalDate().minusYears(51));
        result = roster.isAcceptable(player, reporter);
        assertFalse(result);
        assertNotNull(reporter.getMessage());
        
    }
    
    @Test
    public void rosterGender() {
        Roster roster = new Roster(0, 0, null, null, TeamGender.MALE_ONLY, null);
        ProposedPlayer player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, null);
        boolean result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());

        reporter.clear();
        
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.FEMALE, null);
        result = roster.isAcceptable(player, reporter);
        assertFalse(result);
        assertNotNull(reporter.getMessage());

        reporter.clear();
        
        roster = new Roster(0, 0, null, null, TeamGender.FEMALE_ONLY, null);
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.FEMALE, null);
        result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());

        reporter.clear();
        
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, null);
        result = roster.isAcceptable(player, reporter);
        assertFalse(result);
        assertNotNull(reporter.getMessage());

        reporter.clear();

        roster = new Roster(8, 2, 10, null, TeamGender.CO_ED, 3);
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, null);
        result = roster.isAcceptable(player, reporter);
        assertTrue(result);
        assertNull(reporter.getMessage());

        reporter.clear();

        roster = new Roster(8, 1, 10, null, TeamGender.CO_ED, 3);
        player = new ProposedPlayer(1, new Name("K", "D"), "k@d .com", Gender.MALE, null);
        result = roster.isAcceptable(player, reporter);
        assertFalse(result);
        assertNotNull(reporter.getMessage());
    }
}
