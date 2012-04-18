package com.pastime.leagues.season;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SlugUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.util.PastimeEnvironment;
import com.pastime.util.Principal;
import com.pastime.util.TeamRoles;

public class TeamRepository {

    private JdbcTemplate jdbcTemplate;
    
    private PastimeEnvironment environment;

    private String qualifiedFranchiseAdminSql;
    
    @Inject
    public TeamRepository(JdbcTemplate jdbcTemplate, PastimeEnvironment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
        this.qualifiedFranchiseAdminSql = SqlUtils.sql(new ClassPathResource("qualified-franchise-admin.sql", getClass()));
    }

    @Transactional
    public URI createTeam(TeamForm team, Principal principal) {
        FranchiseAdmin admin = null;
        if (team.getFranchise() != null) {
            admin = findQualifiedFranchiseAdmin(team.getFranchise(), team.getLeague(), principal.getPlayerId());
            team.setName(admin.getFranchiseName());
        }
        Integer number = insertTeam(team);
        makeAdmin(team, number, principal, admin);
        return apiUrl().path("/leagues/{league}/seasons/{season}/teams/{team}").buildAndExpand(team.getLeague(), team.getSeason(), number).toUri();
    }

    public List<PlayerSummary> searchPlayers(TeamKey team, String name, Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    public Team getTeamForEditing(TeamKey teamKey, Principal principal) throws NoSuchAdminException {
        // TODO Auto-generated method stub
        return null;
    }

    // internal helpers

    private FranchiseAdmin findQualifiedFranchiseAdmin(Integer franchiseId, Integer leagueId, Integer playerId) {
        return jdbcTemplate.queryForObject(qualifiedFranchiseAdminSql, new RowMapper<FranchiseAdmin>() {
            public FranchiseAdmin mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new FranchiseAdmin(rs.getString("franchise_name"), (Integer) rs.getObject("number"), rs.getString("nickname"));
            }
        }, franchiseId, leagueId, playerId);
    }

    private Integer insertTeam(TeamForm team) {
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?", team.getLeague(), team.getSeason()); 
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug, franchise) VALUES (?, ?, ?, ?, ?, ?)", team.getLeague(),
                team.getSeason(), number, team.getName(), SlugUtils.toSlug(team.getName()), team.getFranchise());
        return number;
    }
    
    private void makeAdmin(TeamForm team, Integer number, Principal principal, FranchiseAdmin franchiseAdmin) {
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", team.getLeague(), team.getSeason(), number,
                principal.getPlayerId(), franchiseAdmin != null ? franchiseAdmin.getNumber() : null, franchiseAdmin != null ? franchiseAdmin.getNickname() : null);
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)", team.getLeague(), team.getSeason(), number,
                principal.getPlayerId(), TeamRoles.ADMIN);        
    }

    private UriComponentsBuilder apiUrl() {
        return UriComponentsBuilder.fromUri(environment.getApiUrl());
    }
    
 }