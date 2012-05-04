package com.pastime.leagues.season;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.joda.time.DateTime;
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

import com.pastime.players.Gender;
import com.pastime.players.Player;
import com.pastime.players.PlayerMapper;
import com.pastime.util.Name;
import com.pastime.util.PastimeEnvironment;
import com.pastime.util.Range;

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

    private String teamMemberSql;

    private String teamMemberInviteSql;
    
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
        this.teamMemberSql = SqlUtils.sql(new ClassPathResource("team-member.sql", getClass()));
        this.teamMemberInviteSql = SqlUtils.sql(new ClassPathResource("team-member-invite.sql", getClass()));
    }

    public void setInviteGenerator(StringKeyGenerator inviteGenerator) {
        this.inviteGenerator = inviteGenerator;
    }
    
    @Transactional
    public URI createTeam(SeasonKey season, CreateTeamForm team, Integer adminId) {
        if (registrationClosed(season.getLeague(), season.getNumber())) {
            throw new RegistrationClosedException(season);
        }
        FranchiseAdmin franchiseAdmin = null;
        if (team.getFranchise() != null) {
            franchiseAdmin = findQualifiedFranchiseAdmin(team.getFranchise(), season.getLeague(), adminId);
            team.setName(franchiseAdmin.getFranchiseName());
        }
        TeamKey key = insertTeam(season, team);
        makeAdmin(key, adminId, franchiseAdmin != null ? franchiseAdmin.getAttributes() : null);
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

    public AddMemberResult addMember(AddMemberForm form, TeamKey teamKey, Integer adminId) {
        if (form.getRole() == null || form.getRole() == TeamMemberRole.PLAYER) {
            EditableTeam team = getTeamForEditing(teamKey, adminId);
            return team.addOrInvitePlayer(form);
        } else {
            throw new UnsupportedOperationException("Only PLAYER role is supported at this time");
        }
    }
    
    public TeamMember findMember(final TeamKey team, Integer id) {
        return jdbcTemplate.queryForObject(teamMemberSql, new RowMapper<TeamMember>() {
            public TeamMember mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new TeamMember(rs.getInt("id"), TeamMemberRole.PLAYER, new Name(rs.getString("first_name"), rs.getString("last_name")), 
                        (Integer) rs.getObject("number"), rs.getString("nickname"), rs.getString("slug"),
                        Team.api(environment.getApiUrl(), team), Team.site(environment.getSiteUrl(),
                                rs.getString("organization"), rs.getString("league"), rs.getInt("season_number"), rs.getString("season"), rs.getString("team")));
            }
        }, team.getLeague(), team.getSeason(), team.getNumber(), id);
    }

    public TeamMemberInvite findInvite(final TeamKey team, String code) {
        return jdbcTemplate.queryForObject(teamMemberInviteSql, new RowMapper<TeamMemberInvite>() {
            public TeamMemberInvite mapRow(ResultSet rs, int rowNum) throws SQLException {
                Integer player = (Integer) rs.getObject("player_id");
                Name name = mapName(player, rs);                
                return new TeamMemberInvite(rs.getString("code"), name,
                        TeamMemberRole.dbValueOf(rs.getString("role")),
                        new DateTime(rs.getDate("sent")), player, rs.getString("username"),
                        Team.api(environment.getApiUrl(), team), environment.getSiteUrl());
            }
            private Name mapName(Integer player, ResultSet rs) throws SQLException {
                if (player != null) {
                    return new Name(rs.getString("player_first_name"), rs.getString("player_last_name"));
                } else {
                    return Name.valueOf(rs.getString("first_name"), rs.getString("last_name"));
                }                
            }
        }, team.getLeague(), team.getSeason(), team.getNumber(), code);
    }

    @Transactional
    public URI answerInvite(TeamKey team, String code, InviteAnswer answer, Integer playerId) {
        if (answer == null) {
            throw new IllegalArgumentException("answer cannot be null");
        }
        String sql = "SELECT player, sent_by FROM team_member_invites WHERE league = ? AND season = ? AND team = ? AND code = ? AND accepted = false";
        OutstandingInvite invite = jdbcTemplate.queryForObject(sql, new RowMapper<OutstandingInvite>() {
            public OutstandingInvite mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new OutstandingInvite((Integer) rs.getObject("player"), rs.getInt("sent_by"));
            }
        }, team.getLeague(), team.getSeason(), team.getNumber(), code);
        invite.assertInviteFor(playerId);
        if (answer == InviteAnswer.ACCEPT) {
            EditableTeam editableTeam = getTeamForEditing(team, invite.getSentBy());
            URI player = editableTeam.addPlayer(playerId);
            jdbcTemplate.update("UPDATE team_member_invites SET accepted = true, accepted_player = ?, answered_on = ? WHERE league = ? AND season = ? AND team = ? AND code = ?",
                    playerId, new Date(), team.getLeague(), team.getSeason(), team.getNumber(), code);
            return player;
        } else {
            jdbcTemplate.update("UPDATE team_member_invites SET answered_on = ? WHERE league = ? AND season = ? AND team = ? AND code = ?",
                    new Date(), team.getLeague(), team.getSeason(), team.getNumber(), code);
            return null;
        }
    }
    
    @Transactional
    public void removeMemberRole(TeamKey team, Integer memberId, TeamMemberRole role, Integer adminId) {
        assertAdmin(adminId, team);
        jdbcTemplate.update("DELETE FROM team_member_roles WHERE league = ? AND season = ? AND team = ? AND player = ? AND role = ?",
                team.getLeague(), team.getSeason(), team.getNumber(), memberId, TeamMemberRole.dbValue(role));
        if (!hasARole(memberId, team)) {
            jdbcTemplate.update("DELETE FROM team_members WHERE league = ? AND season = ? AND team = ? AND player = ?",
                    team.getLeague(), team.getSeason(), team.getNumber(), memberId);            
        }
    }

    @Transactional
    public void cancelInvite(TeamKey team, String code, Integer adminId) {
        assertAdmin(adminId, team);
        jdbcTemplate.update("DELETE FROM team_member_invites WHERE league = ? AND season = ? AND team = ? AND code = ?",
                team.getLeague(), team.getSeason(), team.getNumber(), code);        
    }

    private void assertAdmin(Integer id, TeamKey team) {
        if (!jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles r WHERE league = ? AND season = ? AND team = ? AND player = ? AND role = 'a')", Boolean.class,
                team.getLeague(), team.getSeason(), team.getNumber(), id)) {
            throw new NoSuchAdminException();
        }
    }
    
    private boolean hasARole(Integer id, TeamKey team) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles r WHERE league = ? AND season = ? AND team = ? AND player = ?)", Boolean.class,
                team.getLeague(), team.getSeason(), team.getNumber(), id);
    }

    // package private used by Team

    boolean isMember(Integer player, TeamKey team) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_members where league = ? AND season = ? AND team = ? AND player = ?)", Boolean.class, 
                team.getLeague(), team.getSeason(), team.getNumber(), player);
    }
    
    void addMember(Integer player, TeamKey team, FranchiseMember franchise) {
        jdbcTemplate.update("INSERT INTO team_members (league, season, team, player, number, nickname) VALUES (?, ?, ?, ?, ?, ?)",
                team.getLeague(), team.getSeason(), team.getNumber(),
                player, franchise != null ? franchise.getNumber() : null, franchise != null ? franchise.getNickname() : null);
    }

    void addTeamMemberRole(TeamKey key, Integer playerId, TeamMemberRole role) {
        jdbcTemplate.update("INSERT INTO team_member_roles (league, season, team, player, role) VALUES (?, ?, ?, ?, ?)",
                key.getLeague(), key.getSeason(), key.getNumber(), playerId, TeamMemberRole.dbValue(role));
    }

    ProposedPlayer findProposedPlayer(Integer id) {
        return jdbcTemplate.queryForObject(proposedPlayerSql, new ProposedPlayerMapper(), id);
    }

    ProposedPlayer findProposedPlayer(String emailAddress) {
        return DataAccessUtils.singleResult(jdbcTemplate.query(proposedPlayerByEmailSql, new ProposedPlayerMapper(), emailAddress));
    }

    boolean alreadyPlaying(Integer playerId, TeamKey key) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM team_member_roles WHERE league = ? and season = ? and team = ? AND player = ? AND role = 'p')",
                Boolean.class, key.getLeague(), key.getSeason(), key.getNumber(), playerId);
    }

    URI sendPlayerInvite(ProposedPlayer player, TeamMember from, EditableTeam team) {
        return sendPersonInvite(new EmailAddress(player.getEmail(), player.getName()), from, team, TeamMemberRole.PLAYER, player.getId());
    }

    URI sendPersonInvite(final EmailAddress email, final TeamMember from, final EditableTeam team, TeamMemberRole role) {
        return sendPersonInvite(email, from, team, role, null);
    }
    
    FranchiseMember findFranchiseMember(Integer playerId, Integer franchise) {
        String sql = "SELECT number, nickname FROM franchise_members WHERE franchise = ? AND player = ?";
        return DataAccessUtils.singleResult(jdbcTemplate.query(sql, new RowMapper<FranchiseMember>() {
            public FranchiseMember mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new FranchiseMember((Integer) rs.getObject("number"), rs.getString("nickname")); 
            }
        }, franchise, playerId));  
    }
    
    // internal helpers

    private boolean registrationClosed(Integer league, Integer season) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM seasons WHERE league = ? and number = ? and registration_status = 'c')", Boolean.class, league, season);
    }

    private FranchiseAdmin findQualifiedFranchiseAdmin(Integer franchiseId, Integer leagueId, Integer playerId) {
        return jdbcTemplate.queryForObject(qualifiedFranchiseAdminSql, new RowMapper<FranchiseAdmin>() {
            public FranchiseAdmin mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new FranchiseAdmin(rs.getString("franchise_name"), new FranchiseMember((Integer) rs.getObject("number"), rs.getString("nickname")));
            }
        }, franchiseId, leagueId, playerId);
    }
        
    private static class FranchiseAdmin {
        
        private String franchiseName;
        
        private FranchiseMember attributes;
        
        public FranchiseAdmin(String franchiseName, FranchiseMember attributes) {
            this.franchiseName = franchiseName;
            this.attributes = attributes;
        }

        public String getFranchiseName() {
            return franchiseName; 
        }
        
        public FranchiseMember getAttributes() {
            return attributes;
        }
        
    }

    private TeamKey insertTeam(SeasonKey season, CreateTeamForm team) {
        Integer number = jdbcTemplate.queryForInt("SELECT coalesce(max(number) + 1, 1) as next_number FROM teams WHERE league = ? AND season = ?",
                season.getLeague(), season.getNumber()); 
        jdbcTemplate.update("INSERT INTO teams (league, season, number, name, slug, franchise) VALUES (?, ?, ?, ?, ?, ?)",
                season.getLeague(), season.getNumber(), number, team.getName(), SlugUtils.toSlug(team.getName()), team.getFranchise());
        return new TeamKey(season, number);
    }
    
    private void makeAdmin(TeamKey key, Integer adminId, FranchiseMember franchiseAdmin) {
        addMember(adminId, key, franchiseAdmin);
        addTeamMemberRole(key, adminId, TeamMemberRole.ADMIN);        
    }

    private EditableTeam getTeamForEditing(final TeamKey teamKey, final Integer adminId) {
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
                EditableTeam team = new EditableTeam(teamKey, rs.getString("name"), rs.getString("sport"), roster, rs.getBoolean("season_rosters_frozen"),
                        (Integer) rs.getObject("franchise"), rs.getString("organization_username"), rs.getString("league_slug"),
                        rs.getInt("season_number"), rs.getString("season_slug"), rs.getString("slug"),
                        environment.getApiUrl(), environment.getSiteUrl(), TeamRepository.this);
                team.setAdmin(rs.getInt("admin_id"), new Name(rs.getString("admin_first_name"), rs.getString("admin_last_name")), 
                        (Integer) rs.getObject("admin_number"), rs.getString("admin_nickname"), rs.getString("admin_slug"));
                return team;
            }
        });
    }

    private URI sendPersonInvite(final EmailAddress email, final TeamMember from, final EditableTeam team, TeamMemberRole role, Integer playerId) {
        final String code = inviteGenerator.generateKey();
        final URI inviteUrl = UriComponentsBuilder.fromUri(team.getApiUrl()).path("/invites/{code}").buildAndExpand(code).toUri();
        jdbcTemplate.update("INSERT INTO team_member_invites (league, season, team, email, role, code, first_name, last_name, sent_by, player) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                team.getLeague(), team.getSeason(), team.getNumber(), email.getValue(), TeamMemberRole.dbValue(role), code,
                email.getFirstName(), email.getLastName(), from.getId(), playerId);
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage message) throws Exception {
               MimeMessageHelper invite = new MimeMessageHelper(message);
               invite.setFrom(new InternetAddress("invites@pastime.com", "Pastime Invites"));
               invite.setTo(new InternetAddress(email.getValue(), email.getName()));
               invite.setSubject("Confirm your " + team.getName() + " team membership");
               Map<String, Object> model = new HashMap<String, Object>(7, 1);
               model.put("name", email.getName() != null ? email.getFirstName() : "Hello");
               model.put("adminUrl", from.getLinks().get("site").toString());               
               model.put("admin", from.getName().toString());
               model.put("sport", team.getSport());              
               model.put("teamUrl", team.getSiteUrl());
               model.put("team", team.getName());
               model.put("inviteUrl", UriComponentsBuilder.fromUri(team.getSiteUrl()).queryParam("invite", code).buildAndExpand(code).toUri());
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

    class OutstandingInvite {

        private Integer player;
        
        private Integer sentBy;

        public OutstandingInvite(Integer player, Integer sentBy) {
            this.player = player;
            this.sentBy = sentBy;
        }

        public void assertInviteFor(Integer playerId) {
            if (!player.equals(playerId)) {
                throw new IllegalArgumentException("This invite is not for you");
            }
        }

        public Integer getSentBy() {
            return sentBy;
        }
        
    }
    
    // cglib ceremony 
    public TeamRepository() {}

}