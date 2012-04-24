SELECT f.name AS franchise_name, m.number, m.nickname
  FROM franchise_member_roles r
    INNER JOIN franchise_members m ON r.franchise = m.franchise AND r.player = m.player
    INNER JOIN franchises f ON m.franchise = f.id
  WHERE r.franchise = ?
    AND r.player = ?
    AND r.role = 'a'
    AND f.sport = (SELECT sport FROM leagues WHERE id = ?);