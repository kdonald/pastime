SELECT p.id, p.first_name, p.last_name, t.nickname, t.slug FROM team_member_roles r 
    INNER JOIN team_members t ON r.team = t.team AND r.player = t.player
    INNER JOIN players p ON t.player = p.id
  WHERE r.league = ? AND r.season = ? AND r.team = ? AND r.player = ? AND r.role = ?