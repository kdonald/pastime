package com.pastime.leagues.season;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SlugUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.leagues.season.AddPlayerForm.EmailAddress;
import com.pastime.players.Player;
import com.pastime.players.PlayerMapper;
import com.pastime.util.Gender;
import com.pastime.util.Name;
import com.pastime.util.PastimeEnvironment;
import com.pastime.util.Range;
import com.pastime.util.TeamRoles;

public class TeamRepository {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedJdbcTemplate;
    
    private PastimeEnvironment environment;

    private String qualifiedFranchiseAdminSql;

    private String playerSearchSql;
    
    private String editTeamSql;

    private String proposedPlayerSql;

    private String proposedPlayerByEmailSql;
    
    @Inject
    public TeamRepository(JdbcTemplate jdbcTemplate, PastimeEnvironment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.environment = environment;
        this.qualifiedFranchiseAdminSql = SqlUtils.sql(new ClassPathResource("qualified-franchise-admin.sql", getClass()));
        this.playerSearchSql = SqlUtils.sql(new ClassPathResource("player-search.sql", getClass()));
        this.editTeamSql = SqlUtils.sql(new ClassPathResource("edit-team.sql", getClass()));
        this.proposedPlayerSql = SqlUtils.sql(new ClassPathResource("proposed-player.sql", getClass()));        
        this.proposedPlayerByEmailSql = SqlUtils.sql(new ClassPathResource("proposed-player-byemail.sql", getClass()));        
    }

    @Transactional
    public URI createTeam(TeamForm team, Integer adminId) {
        if (registrationClosed(team.getLeague(), team.getSeason())) {
            throw new RegistrationClosedException(team.getLeague(), team.getSeason());
        }
        FranchiseMember franchiseAdmin = null;
        if (team.getFranchise() != null) {
            franchiseAdmin = findQualifiedFranchiseAdmin(team.getFranchise(), team.getLeague(), adminId);
            team.setName(franchiseAdmin.getFranchiseName());
        }
        TeamKey key = insertTeam(team);
        makeAdmin(key, adminId, franchiseAdmin);
        return teamApiUrl(key);
    }

    public List<Player> searchPlayers(TeamKey team, String name, Integer adminId) {
        Map<String, Object> params = new HashMap<String, Object>(1, 1);
        params.put("name", name);
        List<Integer> execludes = new ArrayList<Integer>(1);
        execludes.add(adminId);
        params.put("excludes", execludes);
        List<Player> players = namedJdbcTemplate.query(playerSearchSql, params, new PlayerMapper(environment));
        return players;
    }

    public Team getTeamForEditing(final TeamKey teamKey, final Integer adminId) {
        Map<String, Object> params = new HashMap<String, Object>(4, 4);
        params.put("league", teamKey.getLeagueId());
        params.put("season", teamKey.getSeasonId());
        params.put("team", teamKey.getNumber());
        params.put("admin", adminId);
        return namedJdbcTemplate.queryForObject(editTeamSql, params, new RowMapper<Team>() {
            public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
                Roster roster = new Roster(rs.getInt("total_player_count"), rs.getInt("female_player_count"), (Integer) rs.getObject("roster_max"), 
                        new Range((Integer) rs.getObject("age_min"), (Integer) rs.getObject("age_max")),
                        TeamGender.valueOf(rs.getString("gender")), (Integer) rs.getObject("roster_min_female"));
                URI teamSiteUrl = siteUrl().path("/{organization}/{league}/{season}/{team}").buildAndExpand(rs.getString("organization_username"), rs.getString("league_slug"),
                        seasonPath(rs.getInt("season_number"), rs.getString("season_slug")), rs.getString("slug")).toUri();
                TeamMember admin = new TeamMember(rs.getInt("admin_id"), new Name(rs.getString("admin_first_name"), rs.getString("admin_last_name")), 
                        (Integer) rs.getObject("admin_number"), rs.getString("admin_nickname"), rs.getString("admin_slug"), teamSiteUrl);
                return new Team(teamKey, rs.getString("name"), roster, admin, teamApiUrl(teamKey), teamSiteUrl, TeamRepository.this);
            }
            private Object seasonPath(Integer seasonId, String seasonSlug) {
                return seasonSlug != null ? seasonSlug : seasonId;
            }
        });
    }

    // package private used by Team

    void addTeamMember(TeamKey key, Integer playerId, FranchiseMember franchise) {
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", key.getLeagueId(), key.getSeasonId(), key.getNumber(),
                playerId, franchise != null ? franchise.getNumber() : null, franchise != null ? franchise.getNickname() : null);
    }

    void addTeamMemberRole(TeamKey key, Integer playerId, String role) {
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)", key.getLeagueId(), key.getSeasonId(), key.getNumber(),
                playerId, role);
    }

    boolean isTeamMember(TeamKey key, Integer playerId) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members where league = ? AND season = ? AND team = ? AND player = ?)", Boolean.class, 
                key.getLeagueId(), key.getSeasonId(), key.getNumber(), playerId);
    }

    URI teamApiUrl(TeamKey key) {
        return apiUrl().path("/leagues/{league}/seasons/{season}/teams/{number}").buildAndExpand(key.getLeagueId(), key.getSeasonId(), key.getNumber()).toUri();
    }
    
    ProposedPlayer findProposedPlayer(Integer id) {
        return jdbcTemplate.queryForObject(proposedPlayerSql, new ProposedPlayerMapper(), id);
    }

    ProposedPlayer findProposedPlayer(String emailAddress) {
        return DataAccessUtils.singleResult(jdbcTemplate.query(proposedPlayerByEmailSql, new ProposedPlayerMapper(), emailAddress));
    }

    boolean alreadyPlaying(Integer playerId, TeamKey key) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles WHERE league = ? and season = ? and team = ? AND player = ? AND role = 'Player')",
                Boolean.class, key.getLeagueId(), key.getSeasonId(), key.getNumber(), playerId);
    }

    String sendPlayerInvite(ProposedPlayer player, TeamMember from, Team team) {
        return "12345";
    }

    String sendPersonInvite(EmailAddress email, TeamMember from, Team team) {
        return "12345";
    }
    
    // internal helpers

    private boolean registrationClosed(Integer league, Integer season) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM seasons WHERE league = ? and number = ? and registration_status = 'c')", Boolean.class, league, season);
    }

    private FranchiseMember findQualifiedFranchiseAdmin(Integer franchiseId, Integer leagueId, Integer playerId) {
        return jdbcTemplate.queryForObject(qualifiedFranchiseAdminSql, new RowMapper<FranchiseMember>() {
            public FranchiseMember mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new FranchiseMember(rs.getString("franchise_name"), (Integer) rs.getObject("number"), rs.getString("nickname"));
            }
        }, franchiseId, leagueId, playerId);
    }

    private TeamKey insertTeam(TeamForm team) {
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?", team.getLeague(), team.getSeason()); 
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug, franchise) VALUES (?, ?, ?, ?, ?, ?)",
                team.getLeague(), team.getSeason(), number, team.getName(), SlugUtils.toSlug(team.getName()), team.getFranchise());
        return new TeamKey(team.getLeague(), team.getSeason(), number);
    }
    
    private void makeAdmin(TeamKey key, Integer adminId, FranchiseMember franchiseAdmin) {
        addTeamMember(key, adminId, franchiseAdmin);
        addTeamMemberRole(key, adminId, TeamRoles.ADMIN);        
    }
    
    private UriComponentsBuilder apiUrl() {
        return UriComponentsBuilder.fromUri(environment.getApiUrl());
    }
    
    private UriComponentsBuilder siteUrl() {
        return UriComponentsBuilder.fromUri(environment.getSiteUrl());
    }
    
    private static class ProposedPlayerMapper implements RowMapper<ProposedPlayer> {
        public ProposedPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProposedPlayer(rs.getInt("id"), new Name(rs.getString("first_name"), rs.getString("last_name")),
                    rs.getString("email"), Gender.valueOf(rs.getString("gender")), new LocalDate(rs.getDate("birthday")));
        }
    }
    
}