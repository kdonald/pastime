SELECT (SELECT count(*) FROM team_member_roles WHERE league = :league AND season = :season AND team = :team AND role = 'Player') as total_player_count,
  (SELECT count(*) FROM team_member_roles r 
      INNER JOIN players p ON r.player = p.id
    WHERE r.league = :league AND r.season = :season AND r.team = :team AND r.role = 'Player' AND p.gender = 'f') as female_player_count,
  l.roster_min, l.roster_max, l.age_min, l.age_max, l.gender, l.roster_min_female, 
  t.league, t.season, t.number, t.name, t.slug, t.franchise FROM teams t
    INNER JOIN leagues l ON t.league = l.id
  WHERE t.league = :league
    AND t.season = :season
    AND t.number = :team;