SELECT u.username as organization_username,
  l.id as league_id, l.slug as league_slug, l.sport, l.roster_min, l.roster_max, l.age_min, l.age_max, l.gender, l.roster_min_female,
  s.number as season_number, s.slug as season_slug, s.rosters_frozen as season_rosters_frozen,
  t.number, t.name, t.slug, t.franchise,
  (SELECT count(*) FROM team_member_roles WHERE league = :league AND season = :season AND team = :team AND role = 'p') as total_player_count,
  (SELECT count(*) FROM team_member_roles r INNER JOIN players p ON r.player = p.id WHERE r.league = :league AND r.season = :season AND r.team = :team AND r.role = 'Player' AND p.gender = 'f') as female_player_count,  
  p.id as admin_id, p.first_name as admin_first_name, p.last_name as admin_last_name, m.number as admin_number, m.nickname as admin_nickname, m.slug as admin_slug,
  (SELECT email FROM player_emails WHERE player = :admin AND primary_email = true) as admin_email
  FROM team_member_roles r
    INNER JOIN team_members m ON r.league = m.league AND r.season = m.season AND r.team = m.team AND r.player = m.player
    INNER JOIN teams t ON m.league = t.league AND m.season = t.season AND m.team = t.number
    INNER JOIN seasons s ON t.league = s.league AND t.season = s.number
    INNER JOIN leagues l ON s.league = l.id
    INNER JOIN organizations o ON l.organization = o.id
    LEFT OUTER JOIN usernames u ON o.id = u.organization
    INNER JOIN players p ON r.player = p.id
  WHERE r.league = :league
    AND r.season = :season
    AND r.team = :team
    AND r.player = :admin
    AND r.role = 'a';