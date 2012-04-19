package com.pastime.players;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;

import com.pastime.util.Name;
import com.pastime.util.PastimeEnvironment;

public class PlayerMapper implements RowMapper<Player> {
    
    private final PastimeEnvironment environment;
    
    public PlayerMapper(PastimeEnvironment environment) {
        this.environment = environment;
    }

    public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Player(rs.getInt("id"), new Name(rs.getString("first_name"), rs.getString("last_name")),
                Gender.dbValueOf(rs.getString("gender")), new LocalDate(rs.getDate("birthday")), rs.getString("username"),
                environment.getApiUrl(), environment.getSiteUrl());
    }        
}