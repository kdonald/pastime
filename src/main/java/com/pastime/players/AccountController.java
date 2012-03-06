package com.pastime.players;

import static org.springframework.dao.support.DataAccessUtils.singleResult;
import static org.springframework.jdbc.core.SqlStatements.use;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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

import com.pastime.prelaunch.DefaultReferralCodeGenerator;
import com.pastime.prelaunch.ReferralCodeGenerator;

@Controller
public class AccountController {

    private JdbcTemplate jdbcTemplate;
    
    private ReferralCodeGenerator referralCodeGenerator = new DefaultReferralCodeGenerator();

    private StringCookieGenerator cookieGenerator = new StringCookieGenerator("auth_token");
    
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
        Player player = new Player(rs.getInt("id"));
        signinSession(player, response);
        return new ResponseEntity<Object>(player, HttpStatus.OK);           
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
        ReferredBy referredBy = findReferredBy(signupForm.getR());
        Date created = new Date();
        String picture = "http://pastime.com/static/images/default-profile-pic.png";
        Integer playerId = use(jdbcTemplate).insert("INSERT INTO players (first_name, last_name, password, picture, birthday, gender, zip_code, referral_code, referred_by, created) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", "id", Integer.class,
                signupForm.getFirstName(), signupForm.getLastName(), signupForm.getPassword(), picture, signupForm.getBirthday(), signupForm.getGender().name(), signupForm.getZipCode(), referralCode, referredBy != null ? referredBy.getId() : null, created);
        jdbcTemplate.update("INSERT INTO player_emails (email, label, primary_email, player) VALUES (?, ?, ?, ?)", signupForm.getEmail(), "home", true, playerId);
        Player player = new Player(playerId);
        URI url = UriComponentsBuilder.fromHttpUrl("http://pastime.com/players/{id}").buildAndExpand(player.getId()).toUri();
        HttpHeaders headers = new HttpHeaders();    
        headers.setLocation(url);
        signinSession(player, response);
        return new ResponseEntity<Player>(player, headers, HttpStatus.CREATED);
    }

    private void signinSession(Player player, HttpServletResponse response) {
        SecurityContext.setCurrentPlayer(player);
        cookieGenerator.addCookie(player.getId().toString(), response);        
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
