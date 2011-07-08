package com.pastime.teams;

import javax.inject.Inject;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/teams")
public class TeamsController {
	
	private final JdbcTemplate jdbcTemplate;
	
	@Inject
	public TeamsController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@RequestMapping(value="/{name}", method=RequestMethod.GET, produces="application/json")
	public Team get(@PathVariable String name) {
		return jdbcTemplate.queryForObject("select name from Team where name = ?", new TeamRowMapper(), name);
	}

	@RequestMapping(method=RequestMethod.POST)
	public String add(TeamForm team) {
		jdbcTemplate.update("insert into Team (name) values (?)", team.getName());
		return "redirect:/teams/" + team.getName();
	}
	
	@RequestMapping(value="/{name}", method=RequestMethod.POST)
	@Transactional
	public String addMember(@PathVariable String name, TeamMemberForm member) {
		Long teamId = jdbcTemplate.queryForLong("select id from Team where name = ?", name);
		jdbcTemplate.update("insert into TeamMember (team, id, name, email) values (?, (select ifnull(max(id) + 1, 1) from TeamMember where team = ?), ?, ?)", teamId, teamId, member.getName(), member.getEmail());
		return "redirect:/teams/" + name;
	}
	
}
