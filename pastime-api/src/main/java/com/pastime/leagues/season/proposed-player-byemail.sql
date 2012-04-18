SELECT p.id, p.first_name, p.last_name, p.gender, p.birthday, e.email, u.username
  FROM players p
    INNER JOIN player_emails e ON p.id = e.player
    LEFT OUTER JOIN usernames u ON p.id = u.player
  WHERE e.email = ?