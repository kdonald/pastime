SELECT p.id, p.first_name, p.last_name, m.number, m.nickname, m.slug,
  t.slug as team,
  s.number as season_number, s.slug as season,
  l.slug as league,
  u.username as organization
  FROM team_member_roles r 
    INNER JOIN team_members m ON r.league = m.league AND r.season = m.season AND r.team = m.team
    INNER JOIN teams t ON m.league = t.league AND m.season = t.season AND m.team = t.number
    INNER JOIN seasons s ON t.league = s.league AND t.season = s.number
    INNER JOIN leagues l ON s.league = l.id
    INNER JOIN organizations o ON l.organization = o.id
    LEFT OUTER JOIN usernames u ON o.id = u.organization
    INNER JOIN players p ON r.player = p.id
  WHERE r.league = ?
    AND r.season = ?
    AND r.team = ?
    AND r.player = ?
    AND r.role = 'p';