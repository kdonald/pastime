SELECT p.id, p.first_name, p.last_name, p.gender, p.birthday, u.username, f.nickname, f.number, f.joined, f.slug,
  r.role, r.became, r.retired, r.player_captain, r.player_captain_of
  FROM franchise_member_roles r
    INNER JOIN franchise_members f ON r.franchise = f.franchise AND r.player = f.player
    INNER JOIN players p ON f.player = p.id
    LEFT OUTER JOIN usernames u ON p.id = u.player
  WHERE r.franchise = ? AND r.role = ?
  ORDER BY p.last_name;