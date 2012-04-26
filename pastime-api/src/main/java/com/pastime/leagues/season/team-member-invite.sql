SELECT i.code, i.email, i.role, i.first_name, i.last_name, i.sent,
  p.id as player_id, p.first_name as player_first_name, p.last_name as player_last_name,
  u.username
  FROM team_member_invites i
    LEFT OUTER JOIN players p ON i.player = p.id  
    LEFT OUTER JOIN usernames u ON p.id = u.player
  WHERE i.league = ?
    AND i.season = ?
    AND i.team = ?
    AND i.code = ?
