SELECT p.id, p.first_name, p.last_name, p.nickname, p.number, u.username
  FROM players p
    LEFT OUTER JOIN usernames u ON p.id = u.player
  WHERE p.first_name || ' ' || p.last_name LIKE ?
  ORDER BY p.first_name, p.last_name;