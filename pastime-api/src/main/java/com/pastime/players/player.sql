SELECT p.id, p.first_name, p.last_name, p.gender, p.birthday, u.username
  FROM players p
    LEFT OUTER JOIN usernames u ON p.id = u.player
  WHERE p.id = ?;