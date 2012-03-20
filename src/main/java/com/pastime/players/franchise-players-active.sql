SELECT p.id, p.first_name, p.last_name, f.nickname, f.number, f.slug FROM franchise_member_roles r
    INNER JOIN franchise_members f ON r.franchise = f.franchise AND r.player = f.player
    INNER JOIN players p ON f.player = p.id
  WHERE r.franchise = ? AND r.role = 'Player' AND r.player_status = 'a'
  ORDER BY p.last_name;