SELECT f.id, f.name, fu.username, f.founded, f.founder as founder_id, p.first_name as founder_first_name, p.last_name as founder_last_name, pu.username as founder_username
  FROM franchises f
    LEFT OUTER JOIN players p ON f.founder = p.id
    LEFT OUTER JOIN usernames fu ON f.id = fu.franchise
    LEFT OUTER JOIN usernames pu ON f.founder = pu.player
  WHERE f.id = ?
    