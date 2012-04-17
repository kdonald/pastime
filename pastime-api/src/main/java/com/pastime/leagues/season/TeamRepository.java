package com.pastime.leagues.season;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SlugUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.franchises.Franchise;
import com.pastime.franchises.FranchiseMember;
import com.pastime.franchises.FranchiseRepository;
import com.pastime.leagues.season.AddPlayerForm.EmailAddress;
import com.pastime.players.FranchiseSummary;
import com.pastime.players.Player;
import com.pastime.util.Principal;
import com.pastime.util.Range;
import com.pastime.util.TeamRoles;
import com.pastime.util.UserPrincipal;

public class TeamRepository {

    private JdbcTemplate jdbcTemplate;
    
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    private FranchiseRepository franchiseRepository;
    
    private String teamRosterSql;
    
    private RowMapper<Roster> rosterMapper;
    
    private final URI apiUrl;

    public TeamRepository(JdbcTemplate jdbcTemplate, URI apiUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.teamRosterSql = SqlUtils.sql(new ClassPathResource("team-roster.sql", getClass()));
        this.rosterMapper = new RosterMapper();
        this.apiUrl = apiUrl;
    }

    public Player findUser(UserPrincipal principal) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public List<FranchiseSummary> findQualifyingFranchises(Integer league, Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    public Franchise findFranchise(Integer id, Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional
    public URI createTeam(TeamForm team, Principal principal) {
        Integer number = insertTeam(team);
        makeAdmin(team, number, principal);
        return apiUrl().path("/leagues/{league}/seasons/{season}/teams/{team}").buildAndExpand(team.getLeague(), team.getSeason(), number).toUri();
    }
    
    public Team getTeam(TeamKey key) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("league", key.getLeague());
        params.put("season", key.getSeason());
        params.put("team", key.getNumber());
        Roster roster = namedJdbcTemplate.queryForObject(teamRosterSql, params, rosterMapper);
        return new Team(key, roster);
    }

    public Player findTeamMemberByRole(TeamKey key, TeamMemberRole role, Integer userId) {
        return null;
    }
    
    // internal helpers
    

    private UriComponentsBuilder apiUrl() {
        return UriComponentsBuilder.fromUri(apiUrl);
    }
    
    private Integer insertTeam(TeamForm team) {
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?", team.getLeague(), team.getSeason()); 
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug, franchise) VALUES (?, ?, ?, ?, ?, ?)", team.getLeague(),
                team.getSeason(), number, team.getName(), SlugUtils.toSlug(team.getName()), team.getFranchise());
        return number;
    }
    
    private void makeAdmin(TeamForm team, Integer number, Principal principal) {
        FranchiseMember player = null;
        if (team.getFranchise() != null) {
            player = franchiseRepository.findFranchisePlayer(team.getFranchise(), principal.getPlayerId());
        }
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)", team.getLeague(), team.getSeason(), number,
                principal.getPlayerId(), player != null ? player.getNumber() : null, player != null ? player.getNickname() : null);
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)", team.getLeague(), team.getSeason(), number,
                principal.getPlayerId(), TeamRoles.ADMIN);        
    }

    private static class RosterMapper implements RowMapper<Roster> {

        public Roster mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Roster(rs.getInt("total_player_count"), rs.getInt("female_player_count"), (Integer) rs.getObject("roster_max"),
                    Range.valueOf((Integer) rs.getObject("age_min"), (Integer) rs.getObject("age_max")),
                    TeamGender.valueOf(rs.getString("gender")), (Integer) rs.getObject("roster_min_female"));
        }
        
    }

    public List<PlayerSummary> searchPlayers(TeamKey team, String name, Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    public InvitedPlayer sendPersonInvite(EmailAddress email, TeamKey key, Player admin) {
        // TODO Auto-generated method stub
        return null;
    }

    public InvitedPlayer sendPlayerInvite(Player player, TeamKey key, Player admin) {
        // TODO Auto-generated method stub
        return null;
    }

    public Team getTeamForEditing(TeamKey teamKey, Principal principal) throws NoSuchAdminException {
        // TODO Auto-generated method stub
        return null;
    }

 }
