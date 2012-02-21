DROP TABLE next.game_attendance;
DROP TABLE next.games;
DROP SCHEMA next;

CREATE SCHEMA next;
GRANT USAGE ON SCHEMA next TO pastime;

-- Pastime Next Games e.g. Hitmen vs Mergers on Feb 22, 2012
CREATE TABLE next.games (team varchar(16),
  number smallint NOT NULL,
  start_time timestamp NOT NULL,
  opponent varchar(64) NOT NULL,
  CONSTRAINT pk_games PRIMARY KEY (team, number)  
);
  
-- Pastime Next Game Attendance e.g. Keith Donald attending Hitmen game 1 this week
CREATE TABLE next.game_attendance (team varchar(16),
  game smallint,
  player varchar(16),  
  attending boolean,
  update_time timestamp,
  CONSTRAINT pk_game_attendance PRIMARY KEY (team, game, player),
  CONSTRAINT fk_game_attendance_team FOREIGN KEY (team, game) REFERENCES next.games(team, number)
);

GRANT USAGE ON ALL SEQUENCES IN SCHEMA next TO pastime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA next TO pastime;