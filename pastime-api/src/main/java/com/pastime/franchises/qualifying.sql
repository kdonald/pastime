SELECT f.id, f.sport, f.name, f.founded, f.joined, fu.username
  FROM franchises f
    INNER JOIN franchise_member_roles r ON f.id = r.franchise
    LEFT OUTER JOIN usernames fu ON f.id = fu.franchise    
  WHERE r.player = ?
    AND r.role = 'a'
    AND f.sport = (SELECT sport FROM leagues WHERE id = ?)
  ORDER BY f.name;