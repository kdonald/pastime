package com.pastime.leagues.season;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.joda.time.LocalDate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.keygen.InsecureRandomStringGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.templating.StringTemplateLoader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.SlugUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.leagues.season.AddPlayerForm.EmailAddress;
import com.pastime.players.Gender;
import com.pastime.players.Player;
import com.pastime.players.PlayerMapper;
import com.pastime.util.Name;
import com.pastime.util.PastimeEnvironment;
import com.pastime.util.Range;
import com.pastime.util.TeamRoles;

public class TeamRepository {

    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedJdbcTemplate;
    
    private JavaMailSender mailSender;
    
    private StringTemplateLoader templateLoader;
    
    private PastimeEnvironment environment;

    private String qualifiedFranchiseAdminSql;

    private String playerSearchSql;

    private String teamSql;

    private String editTeamSql;

    private String proposedPlayerSql;

    private String proposedPlayerByEmailSql;
    
    private StringKeyGenerator inviteGenerator = new InsecureRandomStringGenerator(6);
    
    @Inject
    public TeamRepository(JdbcTemplate jdbcTemplate, JavaMailSender mailSender, StringTemplateLoader templateLoader, PastimeEnvironment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.mailSender = mailSender;
        this.templateLoader = templateLoader;
        this.environment = environment;
        this.qualifiedFranchiseAdminSql = SqlUtils.sql(new ClassPathResource("qualified-franchise-admin.sql", getClass()));
        this.playerSearchSql = SqlUtils.sql(new ClassPathResource("player-search.sql", getClass()));
        this.teamSql = SqlUtils.sql(new ClassPathResource("team.sql", getClass()));        
        this.editTeamSql = SqlUtils.sql(new ClassPathResource("edit-team.sql", getClass()));
        this.proposedPlayerSql = SqlUtils.sql(new ClassPathResource("proposed-player.sql", getClass()));        
        this.proposedPlayerByEmailSql = SqlUtils.sql(new ClassPathResource("proposed-player-byemail.sql", getClass()));        
    }

    public void setInviteGenerator(StringKeyGenerator inviteGenerator) {
        this.inviteGenerator = inviteGenerator;
    }
    
    @Transactional
    public URI createTeam(SeasonKey season, CreateTeamForm team, Integer adminId) {
        if (registrationClosed(season.getLeague(), season.getNumber())) {
            throw new RegistrationClosedException(season);
        }
        FranchiseMember franchiseAdmin = null;
        if (team.getFranchise() != null) {
            franchiseAdmin = findQualifiedFranchiseAdmin(team.getFranchise(), season.getLeague(), adminId);
            team.setName(franchiseAdmin.getFranchiseName());
        }
        TeamKey key = insertTeam(season, team);
        makeAdmin(key, adminId, franchiseAdmin);
        return Team.api(environment.getApiUrl(), key);
    }

    public Team findTeam(final TeamKey key) {
        return jdbcTemplate.queryForObject(teamSql, new RowMapper<Team>() {
            public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Team(key, rs.getString("name"), rs.getString("organization_username"), rs.getString("league_slug"),
                        rs.getString("season_slug"), rs.getString("slug"), (Integer) rs.getObject("franchise"),
                        environment.getApiUrl(), environment.getSiteUrl());
            }
        }, key.getLeague(), key.getSeason(), key.getNumber());
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

    public EditableTeam getTeamForEditing(final TeamKey teamKey, final Integer adminId) {
        Map<String, Object> params = new HashMap<String, Object>(4, 4);
        params.put("league", teamKey.getLeague());
        params.put("season", teamKey.getSeason());
        params.put("team", teamKey.getNumber());
        params.put("admin", adminId);
        return namedJdbcTemplate.queryForObject(editTeamSql, params, new RowMapper<EditableTeam>() {
            public EditableTeam mapRow(ResultSet rs, int rowNum) throws SQLException {
                Roster roster = new Roster(rs.getInt("total_player_count"), rs.getInt("female_player_count"), (Integer) rs.getObject("roster_max"), 
                        new Range((Integer) rs.getObject("age_min"), (Integer) rs.getObject("age_max")),
                        TeamGender.valueOf(rs.getString("gender")), (Integer) rs.getObject("roster_min_female"));
                URI teamSiteUrl = Team.site(environment.getSiteUrl(), rs.getString("organization_username"), rs.getString("league_slug"), rs.getInt("season_number"), rs.getString("season_slug"), rs.getString("slug"));
                TeamMember admin = new TeamMember(rs.getInt("admin_id"), new Name(rs.getString("admin_first_name"), rs.getString("admin_last_name")), 
                        (Integer) rs.getObject("admin_number"), rs.getString("admin_nickname"), rs.getString("admin_slug"), teamSiteUrl);
                return new EditableTeam(teamKey, rs.getString("name"), rs.getString("sport"), roster, admin,
                        Team.api(environment.getApiUrl(), teamKey), teamSiteUrl,
                        TeamRepository.this);
            }
        });
    }

    // package private used by Team

    void addTeamMember(TeamKey key, Integer playerId, FranchiseMember franchise) {
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)",
                key.getLeague(), key.getSeason(), key.getNumber(),
                playerId, franchise != null ? franchise.getNumber() : null, franchise != null ? franchise.getNickname() : null);
    }

    void addTeamMemberRole(TeamKey key, Integer playerId, String role) {
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)",
                key.getLeague(), key.getSeason(), key.getNumber(), playerId, role);
    }

    boolean isTeamMember(TeamKey key, Integer playerId) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members where league = ? AND season = ? AND team = ? AND player = ?)", Boolean.class, 
                key.getLeague(), key.getSeason(), key.getNumber(), playerId);
    }

    ProposedPlayer findProposedPlayer(Integer id) {
        return jdbcTemplate.queryForObject(proposedPlayerSql, new ProposedPlayerMapper(), id);
    }

    ProposedPlayer findProposedPlayer(String emailAddress) {
        return DataAccessUtils.singleResult(jdbcTemplate.query(proposedPlayerByEmailSql, new ProposedPlayerMapper(), emailAddress));
    }

    boolean alreadyPlaying(Integer playerId, TeamKey key) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles WHERE league = ? and season = ? and team = ? AND player = ? AND role = 'Player')",
                Boolean.class, key.getLeague(), key.getSeason(), key.getNumber(), playerId);
    }

    URI sendPlayerInvite(final ProposedPlayer player, final TeamMember from, final EditableTeam team) {
        return sendPersonInvite(new EmailAddress(player.getEmail(), player.getName()), from, team, player.getId());
    }

    URI sendPersonInvite(final EmailAddress email, final TeamMember from, final EditableTeam team) {
        return sendPersonInvite(email, from, team, null);
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

    private TeamKey insertTeam(SeasonKey season, CreateTeamForm team) {
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?",
                season.getLeague(), season.getNumber()); 
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug, franchise) VALUES (?, ?, ?, ?, ?, ?)",
                season.getLeague(), season.getNumber(), number, team.getName(), SlugUtils.toSlug(team.getName()), team.getFranchise());
        return new TeamKey(season, number);
    }
    
    private void makeAdmin(TeamKey key, Integer adminId, FranchiseMember franchiseAdmin) {
        addTeamMember(key, adminId, franchiseAdmin);
        addTeamMemberRole(key, adminId, TeamRoles.ADMIN);        
    }
    
    private URI sendPersonInvite(final EmailAddress email, final TeamMember from, final EditableTeam team, Integer playerId) {
        String code = inviteGenerator.generateKey();
        final URI inviteUrl = UriComponentsBuilder.fromUri(team.getApiUrl()).path("/invites/{code}").buildAndExpand(code).toUri();
        jdbcTemplate.update("INSERT INTO team_member_invites (league, season, team, email, role, code, sent_by, player) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                team.getLeague(), team.getSeason(), team.getNumber(), email.getValue(), TeamRoles.PLAYER, code, from.getId(), playerId);
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(email.getValue(), email.getDisplayName().toString()));
               invite.setSubject("Confirm your " + team.getName() + " team membership");
               Map<String, Object> model = new HashMap<String, Object>(7, 1);
               model.put("name", email.getDisplayName() != null ? email.getDisplayName().getFirstName() : "Hello");
               model.put("adminUrl", from.getSiteUrl().toString());               
               model.put("admin", from.getName().toString());
               model.put("sport", team.getSport());              
               model.put("teamUrl", team.getSiteUrl());
               model.put("team", team.getName());
               model.put("inviteUrl", inviteUrl.toString());
               invite.setText(templateLoader.getTemplate("teams/mail/player-invite").render(model), true);
            }
         };
         mailSender.send(preparator);
         return inviteUrl;
    }
    
    private static class ProposedPlayerMapper implements RowMapper<ProposedPlayer> {
        public ProposedPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProposedPlayer(rs.getInt("id"), new Name(rs.getString("first_name"), rs.getString("last_name")),
                    rs.getString("email"), Gender.dbValueOf(rs.getString("gender")), new LocalDate(rs.getDate("birthday")));
        }
    }
    
    // cglib ceremony 
    public TeamRepository() {}

}