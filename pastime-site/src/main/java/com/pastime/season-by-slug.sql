SELECT s.name, s.start_date, l.roster_min, l.roster_max, l.id as league_id, s.number
  FROM seasons s
    INNER JOIN leagues l ON s.league = l.id
  WHERE s.league = (SELECT id FROM leagues where organization = (SELECT organization FROM usernames WHERE username = ?) and slug = ?) and s.slug = ?