SELECT o.id as organization_id, o.name as organization_name, u.username as organization_username,
       l.id as league_id, l.slug as league_slug, l.sport as league_sport, l.format as league_format, l.nature as league_nature,
       s.number as season_number, s.name as season_name, s.slug as season_slug, s.start_date as season_start_date,
       v.venue_id, v.venue_name, v.venue_latitude, v.venue_longitude
  FROM seasons s
    INNER JOIN leagues l ON s.league = l.id
    INNER JOIN organizations o ON l.organization = o.id
    INNER JOIN (SELECT l.league, l.venue as venue_id, v.name as venue_name, v.latitude as venue_latitude, v.longitude as venue_longitude
                  FROM league_venues l
                    INNER JOIN venues v ON l.venue = v.id
                  WHERE l.primary_venue = true) v ON l.id = v.league
    LEFT OUTER JOIN usernames u ON o.id = u.organization
  WHERE s.status = 'o'