DROP TABLE next.game_attendance;
DROP TABLE next.games;
DROP SCHEMA next;

CREATE SCHEMA next;
GRANT USAGE ON SCHEMA next TO pastime;

-- Pastime Next Games e.g. Hitmen vs Mergers on Feb 22, 2012
CREATE TABLE next.games (team_slug varchar(16),
  number smallint NOT NULL,
  start_time timestamp NOT NULL,
  opponent varchar(64) NOT NULL,
  game bigint NOT NULL,
  CONSTRAINT pk_games PRIMARY KEY (team_slug, number),
  CONSTRAINT fk_games_game FOREIGN KEY (game) REFERENCES games(id)  
);
  
-- Pastime Next Game Attendance e.g. Keith Donald attending Hitmen game 1 this week
CREATE TABLE next.game_attendance (team_slug varchar(16),
  game smallint,
  registered_player_slug varchar(16),
  attending boolean,
  update_time timestamp,
  name varchar(64),  
  player integer NOT NULL,
  CONSTRAINT pk_game_attendance PRIMARY KEY (team_slug, game, registered_player_slug),
  CONSTRAINT fk_game_attendance_game FOREIGN KEY (team_slug, game) REFERENCES next.games(team_slug, number),
  CONSTRAINT fk_game_attendance_player FOREIGN KEY (player) REFERENCES players(id)  
);

GRANT USAGE ON ALL SEQUENCES IN SCHEMA next TO pastime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA next TO pastime;