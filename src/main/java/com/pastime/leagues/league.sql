SELECT o.id as organization_id, o.name as organization_name,
       l.id as league_id, l.sport as league_sport, l.format as league_format, l.nature as league_nature,
       l.roster_min, l.roster_max,
       s.number as season_number, s.name as season_name, s.start_date as season_start_date,
       v.venue_id, v.venue_name, v.venue_latitude, v.venue_longitude
  FROM seasons s
    INNER JOIN leagues l ON s.league = l.id
    INNER JOIN organizations o ON l.organization = o.id
    INNER JOIN (SELECT l.league, l.venue as venue_id, v.name as venue_name, v.latitude as venue_latitude, v.longitude as venue_longitude
                  FROM league_venues l
                    INNER JOIN venues v ON l.venue = v.id
                  WHERE l.primary_venue = true) v ON l.id = v.league
  WHERE s.league = (SELECT id FROM leagues where organization = (SELECT organization FROM usernames WHERE username = ?) and slug = ?) and s.number = ?