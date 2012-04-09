package com.pastime.leagues;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.pastime.util.Range;

public class TeamRepository {

    private NamedParameterJdbcTemplate jdbcTemplate;

    private String teamRosterSql;
    
    private RowMapper<Roster> rosterMapper;
    
    public TeamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.teamRosterSql = SqlUtils.sql(new ClassPathResource("team-roster.sql", getClass()));
        this.rosterMapper = new RosterMapper();
    }

    public Team findTeam(Integer league, Integer season, Integer number) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("league", league);
        params.put("season", season);
        params.put("team", number);
        Roster roster = jdbcTemplate.queryForObject(teamRosterSql, params, rosterMapper);
        return new Team(league, season, number, roster);
    }
    
    private static class RosterMapper implements RowMapper<Roster> {

        public Roster mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Roster(rs.getInt("total_player_count"), rs.getInt("female_player_count"), (Integer) rs.getObject("roster_max"),
                    Range.valueOf((Integer) rs.getObject("age_min"), (Integer) rs.getObject("age_max")),
                    TeamGender.valueOf(rs.getString("gender")), (Integer) rs.getObject("roster_min_female"));
        }
        
    }
}
