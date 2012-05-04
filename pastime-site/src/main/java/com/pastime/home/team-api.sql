SELECT l.id as league, s.number as season, t.number as team
  FROM teams t
  	INNER JOIN seasons s ON t.league = s.league AND t.season = s.number
    INNER JOIN leagues l ON s.league = l.id
  WHERE t.league = (SELECT id FROM leagues WHERE organization = (SELECT organization FROM usernames WHERE username = ?) AND slug = ?)
  	AND s.slug = ?
  	AND t.slug = ?