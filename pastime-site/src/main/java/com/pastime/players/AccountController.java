package com.pastime.players;

import static org.springframework.dao.support.DataAccessUtils.singleResult;
import static org.springframework.jdbc.core.SqlStatements.use;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringCookieGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import com.pastime.util.DefaultReferralCodeGenerator;
import com.pastime.util.ErrorBody;
import com.pastime.util.Name;
import com.pastime.util.Principal;
import com.pastime.util.ReferralCodeGenerator;
import com.pastime.util.SecurityContext;
import com.sun.security.auth.UserPrincipal;

@Controller
public class AccountController {

    private String siteUrl = "http://localhost:8080/";
    
    private JdbcTemplate jdbcTemplate;
    
    private ReferralCodeGenerator referralCodeGenerator = new DefaultReferralCodeGenerator();

    private StringCookieGenerator cookieGenerator = new StringCookieGenerator("access_token");
    
    public AccountController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value="/signin", method=RequestMethod.GET)
    public String signin() {
        return "players/signin";
    }
    
    @RequestMapping(value="/signin", method=RequestMethod.POST, produces="application/json")
    public ResponseEntity<? extends Object> signin(@Valid SigninForm form, HttpServletResponse response) {
        SqlRowSet rs;
        if (form.isEmail()) {
            rs = jdbcTemplate.queryForRowSet("select p.id, p.password FROM player_emails e inner join players p on e.player = p.id WHERE e.email = ?", form.getName());
            if (!rs.last()) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("email not on file"), HttpStatus.BAD_REQUEST);               
            }
        } else {
            rs = jdbcTemplate.queryForRowSet("select username_type, player, team, league FROM usernames where username = ?", form.getName());
            if (!rs.last()) {
                return new ResponseEntity<ErrorBody>(new ErrorBody("username not on file"), HttpStatus.BAD_REQUEST);               
            }
            String type = rs.getString("username_type");
            if ("p".equals(type)) {
                rs = jdbcTemplate.queryForRowSet("select p.id, p.password FROM player p where p.id = ?", rs.getInt("player"));
            } else {
                throw new UnsupportedOperationException("Not yet supported");
            }
        }
        String password = rs.getString("password");
        if (!password.equals(form.getPassword())) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("password doesn't match"), HttpStatus.BAD_REQUEST);               
        }
        Principal principal = new Principal(rs.getInt("id"));
        signinSession(principal, response);
        return new ResponseEntity<Object>(principal, HttpStatus.OK);           
    }

    @RequestMapping(value="/signup", method=RequestMethod.GET)
    public String signup() {
        return "players/signup";
    }

    @RequestMapping(value="/signup", method=RequestMethod.POST, produces="application/json")
    @Transactional
    public ResponseEntity<? extends Object> signup(@Valid SignupForm signupForm, HttpServletResponse response) {
        if (playerExists(signupForm.getEmail())) {
            return new ResponseEntity<ErrorBody>(new ErrorBody("email already exists"), HttpStatus.BAD_REQUEST);
        }
        String referralCode = generateUniqueReferralCode();
        ReferredBy referredBy = findReferredBy(signupForm.getReferral_code());
        Integer playerId = use(jdbcTemplate).insert("INSERT INTO players (first_name, last_name, password, birthday, gender, zip_code, referral_code, referred_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", "id", Integer.class,
                signupForm.getFirst_name(), signupForm.getLast_name(), signupForm.getPassword(), signupForm.getBirthday(), signupForm.getGender().getCode(), signupForm.getZip_code(), referralCode, referredBy != null ? referredBy.getId() : null);
        jdbcTemplate.update("INSERT INTO player_emails (email, label, primary_email, player) VALUES (?, ?, ?, ?)", signupForm.getEmail(), "home", true, playerId);
        Principal principal = new Principal(playerId);
        URI url = UriComponentsBuilder.fromHttpUrl(siteUrl + "/players/{id}").buildAndExpand(principal.getPlayerId()).toUri();
        HttpHeaders headers = new HttpHeaders();    
        headers.setLocation(url);
        signinSession(principal, response);
        Map<String, Object> accessGrant = new HashMap<String, Object>();
        accessGrant.put("access_token", principal.getPlayerId());
        return new ResponseEntity<Map<String, Object>>(accessGrant, headers, HttpStatus.CREATED);
    }
    
    private void signinSession(Principal principal, HttpServletResponse response) {
        SecurityContext.setPrincipal(principal);
        cookieGenerator.addCookie(principal.getPlayerId().toString(), response);        
    }
    
    private boolean playerExists(String email) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM player_emails WHERE email = ?)", Boolean.class, email);        
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
    
    // cglib ceremony
    public AccountController() {}
}
