package com.pastime.franchises;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlUtils;
import org.springframework.transaction.annotation.Transactional;

import com.pastime.players.PictureType;
import com.pastime.players.Player;
import com.pastime.players.PlayerMapper;
import com.pastime.util.PastimeEnvironment;

public class FranchiseRepository {
    
    private JdbcTemplate jdbcTemplate;

    private PastimeEnvironment environment;

    private String qualifyingFranchisesSql;

    private String franchiseSql;
    
    private String currentMembersOfRoleSql;
    
    public FranchiseRepository(JdbcTemplate jdbcTemplate, PastimeEnvironment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
        this.qualifyingFranchisesSql = SqlUtils.sql(new ClassPathResource("qualifying.sql", getClass()));        
        this.franchiseSql = SqlUtils.sql(new ClassPathResource("franchise.sql", getClass()));
        this.currentMembersOfRoleSql = SqlUtils.sql(new ClassPathResource("members-current-role.sql", getClass()));
    }

    public List<Franchise> findQualifyingFranchises(Integer leagueId, Integer playerId) {
        return jdbcTemplate.query(qualifyingFranchisesSql, new FranchiseMapper(), playerId, leagueId);        
    }

    public Franchise findFranchise(Integer id) {
        return jdbcTemplate.queryForObject(franchiseSql, new FranchiseMapper(), id);
    }

    public URI findPicture(Integer franchiseId, PictureType type) {
        throw new UnsupportedOperationException("Not yet implemented");        
    }

    @Transactional
    public List<FranchiseMember> findFranchiseMembers(Integer franchiseId, MemberRole role, MemberStatus status) {
        String franchiseUsername = findFranchiseUsername(franchiseId);
        if (role == MemberRole.ALL) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            if (status == MemberStatus.CURRENT) {
                return jdbcTemplate.query(currentMembersOfRoleSql, new SingleRoleFranchiseMemberMapper(franchiseId, franchiseUsername),
                        franchiseId, role.getValue());
            } else if (status == MemberStatus.RETIRED) {
                throw new UnsupportedOperationException("Not yet implemented");
            } else {
                throw new UnsupportedOperationException("Not yet implemented");                
            }
        }
    }

    @Transactional
    public FranchiseMember findFranchiseMember(Integer franchiseId, Integer playerId) {
        throw new UnsupportedOperationException("Not yet implemented");        
    }
    
    public URI findFranchiseMemberPicture(Integer franchiseId, Integer playerId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public URI findFounder(Integer franchiseId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    // internal helpers
    
    private String findFranchiseUsername(Integer franchiseId) {
        return jdbcTemplate.queryForObject("select u.username FROM franchises f LEFT OUTER JOIN usernames u ON f.id = u.franchise WHERE f.id = ?",
                String.class, franchiseId);        
    }
    
    private class FranchiseMapper implements RowMapper<Franchise> {
        public Franchise mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Franchise(rs.getInt("id"), rs.getString("name"), rs.getString("sport"),
                    localDate(rs.getDate("founded")), new LocalDate(rs.getDate("joined")),
                    rs.getString("username"), environment.getApiUrl(), environment.getSiteUrl());
        }
    }
    
    private class SingleRoleFranchiseMemberMapper implements RowMapper<FranchiseMember> {
        
        private Integer franchiseId;
        
        private String franchiseUsername;
        
        public SingleRoleFranchiseMemberMapper(Integer franchiseId, String franchiseUsername) {
            this.franchiseId = franchiseId;
            this.franchiseUsername = franchiseUsername;
        }

        public FranchiseMember mapRow(ResultSet rs, int rowNum) throws SQLException {
            PlayerMapper playerMapper = new PlayerMapper(environment);
            Player player = playerMapper.mapRow(rs, rowNum);
            FranchiseMember member = new FranchiseMember(player, (Integer) rs.getObject("number"), rs.getString("nickname"),
                    new LocalDate(rs.getDate("joined")), rs.getString("slug"),
                    Franchise.api(environment.getApiUrl(), franchiseId), Franchise.site(environment.getSiteUrl(), franchiseId, franchiseUsername));
            String role = rs.getString("role");
            if ("Player".equals(role)) {
                member.getRoles().put("Player", new PlayerMemberRole(new LocalDate(rs.getDate("became")),
                        localDate(rs.getDate("retired")), rs.getBoolean("player_captain"), rs.getString("player_captain_of")));
            } else {
                member.getRoles().put(role, new FranchiseMemberRole(new LocalDate(rs.getDate("became")),
                        localDate(rs.getDate("retired"))));
            }
            return member;
        }
        
    }
    
    private LocalDate localDate(Date date) {
        return date != null ? new LocalDate(date) : null;
    }
    
}