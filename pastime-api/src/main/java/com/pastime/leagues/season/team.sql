SELECT u.username as organization_username,
  l.slug as league_slug,
  s.slug as season_slug,
  t.name, t.slug, t.franchise
  FROM teams t
    INNER JOIN seasons s ON t.league = s.league AND t.season = s.number
    INNER JOIN leagues l ON s.league = l.id
    INNER JOIN organizations o ON l.organization = o.id
    LEFT OUTER JOIN usernames u ON o.id = u.organization
  WHERE t.league = ?
    AND t.season = ?
    AND t.number = ?