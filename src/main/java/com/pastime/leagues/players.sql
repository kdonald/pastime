SELECT m.player as id, m.slug, m.picture, m.number, m.nickname
  FROM team_members m
    INNER JOIN team_member_roles r ON m.league = r.league AND m.season = r.season AND m.team = r.team AND m.player = r.player
  WHERE m.league = 1 AND m.season = 1 AND m.team = 1 AND m.picture IS NOT NULL AND r.role = 'Player'
  ORDER BY RANDOM() LIMIT 10;