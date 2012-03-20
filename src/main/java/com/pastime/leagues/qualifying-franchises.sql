SELECT f.id, f.name
  FROM franchises f
    INNER JOIN franchise_member_roles r ON f.id = r.franchise
  WHERE r.player = ? AND r.role = 'Admin' AND f.sport = (SELECT sport FROM leagues WHERE id = ?)
  ORDER BY f.name;