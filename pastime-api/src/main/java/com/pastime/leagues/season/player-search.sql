SELECT p.id, p.first_name, p.last_name, p.gender, p.birthday, u.username
  FROM players p
    LEFT OUTER JOIN usernames u ON p.id = u.player
  WHERE p.first_name || ' ' || p.last_name ILIKE :name || '%'
  AND p.id NOT IN (:excludes)
  ORDER BY p.first_name, p.last_name;