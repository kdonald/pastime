package com.pastime.players;

import static org.springframework.dao.support.DataAccessUtils.singleResult;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlStatements;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.prelaunch.DefaultReferralCodeGenerator;
import com.pastime.prelaunch.ReferralCodeGenerator;

@Controller
public class SignupController {

    private ReferralCodeGenerator referralCodeGenerator = new DefaultReferralCodeGenerator();

    private JdbcTemplate jdbcTemplate;
    
    public SignupController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value="/signup", method=RequestMethod.GET)
    public String signup() {
        return "players/signup";
    }

    @RequestMapping(value="/signup", method=RequestMethod.POST)
    public ResponseEntity<Integer> signup(@Valid SignupForm signupForm) {
        String referralCode = generateUniqueReferralCode();
        ReferredBy referredBy = findReferredBy(signupForm.getR());
        Date created = new Date();
        Integer playerId = SqlStatements.use(jdbcTemplate).insert("INSERT INTO players (email, first_name, last_name, birthday, gender, zip_code, referral_code, referred_by, created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "id", Integer.class, signupForm.getEmail(), signupForm.getFirstName(), signupForm.getLastName(), signupForm.getBirthday(), signupForm.getGender().name(), signupForm.getZipCode(), referralCode, referredBy != null ? referredBy.getId() : null, created);
        URI url = UriComponentsBuilder.fromHttpUrl("http://pastimebrevard.com/players/{id}").buildAndExpand(playerId).toUri();
        HttpHeaders headers = new HttpHeaders();    
        headers.setLocation(url);
        return new ResponseEntity<Integer>(playerId, headers, HttpStatus.CREATED);
    }
    
    private ReferredBy findReferredBy(String r) {
        if (!StringUtils.hasText(r)) {
            return null;
        }
        String sql = "SELECT id, first_name, last_name, email, referral_code FROM players WHERE referral_code = ?";
        return singleResult(jdbcTemplate.query(sql,
                new RowMapper<ReferredBy>() {
                    public ReferredBy mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new ReferredBy(rs.getInt("id"), new Name(rs.getString("first_name"), rs.getString("last_name")), rs.getString("email"), rs.getString("referral_code"));
                    }
                }, r));
    }
    
    private String generateUniqueReferralCode() {
        do {
            String referralCode = referralCodeGenerator.generateKey();
            if (!jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM players WHERE referral_code = ?)", Boolean.class, referralCode)) {
                return referralCode;
            }
        } while (true);
    }
}
