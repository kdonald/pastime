SELECT f.id, f.name, f.sport, f.founded, f.joined, fu.username
  FROM franchises f
    LEFT OUTER JOIN usernames fu ON f.id = fu.franchise
  WHERE f.id = ?
    