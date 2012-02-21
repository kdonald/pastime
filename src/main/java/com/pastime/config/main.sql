DROP TABLE games;
DROP TABLE registered_players;
DROP TABLE registered_teams;
DROP TABLE seasons;
DROP TABLE leagues;
DROP TABLE team_players;
DROP TABLE teams;
DROP TABLE players;

-- Pastime Players e.g. Keith Donald, Alexander Weaver, etc.
CREATE TABLE players (id serial CONSTRAINT pk_players PRIMARY KEY,
  email varchar(320) NOT NULL UNIQUE,
  first_name varchar(64) NOT NULL,
  last_name varchar(64) NOT NULL,
  number smallint,
  nickname varchar(16),
  referral_code char(6) NOT NULL UNIQUE,
  referred_by integer,  
  created timestamp NOT NULL CONSTRAINT df_players_created DEFAULT now(),
  deleted boolean NOT NULL CONSTRAINT df_players_deleted DEFAULT false,  
  CONSTRAINT fk_players_referred_by FOREIGN KEY (referred_by) REFERENCES players(id)  
);
  
-- Pastime Franchises e.g. Hitmen
CREATE TABLE teams (id serial CONSTRAINT pk_teams PRIMARY KEY,
  name varchar(64) NOT NULL,
  slug varchar(16) NOT NULL UNIQUE,
  founded date,
  founded_by integer,
  founded_by_name varchar(128),
  created timestamp NOT NULL CONSTRAINT df_teams_created DEFAULT now(),
  created_by integer NOT NULL,
  CONSTRAINT fk_teams_founded_by FOREIGN KEY (founded_by) REFERENCES players(id),
  CONSTRAINT fk_teams_created_by FOREIGN KEY (created_by) REFERENCES players(id)
);

-- Pastime "Franchise" Players e.g. Hitmen Keith Donald, Alexander Weaver, Brian Fisher, etc.
CREATE TABLE team_players (id bigserial CONSTRAINT pk_team_players PRIMARY KEY,
  number smallint NOT NULL UNIQUE,
  nickname varchar(16) UNIQUE,
  team integer NOT NULL,
  player integer NOT NULL,
  CONSTRAINT fk_team_players_team FOREIGN KEY (team) REFERENCES teams(id),
  CONSTRAINT fk_team_players_player FOREIGN KEY (player) REFERENCES players(id)  
);

-- Pastime Leagues e.g. South Brevard Adult Flag Football
CREATE TABLE leagues (id serial CONSTRAINT pk_leagues PRIMARY KEY,
  name varchar(64) NOT NULL,
  created timestamp NOT NULL CONSTRAINT df_leagues_created DEFAULT now()  
);

-- Pastime League Seasons e.g. South Brevard Adult Flag Football Winter 2012
CREATE TABLE seasons (id bigserial CONSTRAINT pk_seasons PRIMARY KEY,
  name varchar(64) NOT NULL,
  league integer NOT NULL,
  start_date date,
  status char(1), -- N: new, O: registration open, C: registration closed, R: running, X: over
  created timestamp NOT NULL CONSTRAINT df_seasons_created DEFAULT now(),  
  CONSTRAINT fk_seasons_league FOREIGN KEY (league) REFERENCES leagues(id)
);

-- Pastime Team Registrations e.g. Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE registered_teams (id bigserial CONSTRAINT pk_registered_teams PRIMARY KEY,
  team integer NOT NULL,
  league integer NOT NULL,
  season bigint NOT NULL,
  CONSTRAINT fk_registered_teams_team FOREIGN KEY (team) REFERENCES teams(id),
  CONSTRAINT fk_registered_teams_league FOREIGN KEY (league) REFERENCES leagues(id),  
  CONSTRAINT fk_registered_teams_season FOREIGN KEY (season) REFERENCES seasons(id)  
);

-- Pastime Player Registrations e.g. Keith Donald - Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE registered_players (id bigserial CONSTRAINT pk_registered_players PRIMARY KEY,
  team bigint NOT NULL,
  player integer NOT NULL,
  number smallint NOT NULL UNIQUE,
  nickname varchar(16) UNIQUE,
  CONSTRAINT fk_registered_players_team FOREIGN KEY (team) REFERENCES registered_teams(id),
  CONSTRAINT fk_registered_players_player FOREIGN KEY (player) REFERENCES players(id)    
);

-- Pastime Games e.g. Week 1 - Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE games (id bigserial CONSTRAINT pk_games PRIMARY KEY,
  team bigint NOT NULL,
  number smallint NOT NULL,  
  opponent bigint NOT NULL,
  start_time timestamp NOT NULL,
  CONSTRAINT fk_games_team FOREIGN KEY (team) REFERENCES registered_teams(id),
  CONSTRAINT fk_games_opponent FOREIGN KEY (opponent) REFERENCES registered_teams(id)  
);