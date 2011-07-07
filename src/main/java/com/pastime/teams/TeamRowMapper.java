package com.pastime.teams;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

class TeamRowMapper implements RowMapper<Team> {
	public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new Team(rs.getString("name"));
	}		
}