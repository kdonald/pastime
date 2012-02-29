DROP TABLE usernames;
DROP TABLE games;
DROP TABLE registered_team_sponsorship;
DROP TABLE registered_players;
DROP TABLE registered_team_payments;
DROP TABLE registered_teams;
DROP TABLE free_agents;
DROP TABLE seasons;
DROP TABLE league_staff;
DROP TABLE league_venues;
DROP TABLE leagues;
DROP TABLE sponsors;
DROP TABLE venues;
DROP TABLE organizations;
DROP TABLE team_players;
DROP TABLE teams;
DROP TABLE player_children;
DROP TABLE player_sports;
DROP TABLE player_phones;
DROP TABLE player_emails;
DROP TABLE players;
DROP TABLE format_rules;
DROP TABLE formats;
DROP TABLE sports;

-- Pastime Sports e.g. Flag Football --
CREATE TABLE sports (id serial CONSTRAINT pk_sports PRIMARY KEY,
  name varchar(64) NOT NULL UNIQUE,
  logo varchar(256)
);

-- Pastime Sport Formats e.g. 7 on 7 Flag Football --
CREATE TABLE formats (id serial CONSTRAINT pk_formats PRIMARY KEY,
  name varchar(64) NOT NULL
);

-- Pastime Format Rules e.g. Games are 12 minutes long. --
CREATE TABLE format_rules (format integer,
  number smallint,
  title varchar(64),
  description varchar(1024),
  CONSTRAINT pk_format_rules PRIMARY KEY (format, number),
  CONSTRAINT fk_format_rules_format FOREIGN KEY (format) REFERENCES formats(id)  
);

-- Pastime Players e.g. Keith Donald, Alexander Weaver, Jason Konicki, etc.
CREATE TABLE players (id serial CONSTRAINT pk_players PRIMARY KEY,
  first_name varchar(64) NOT NULL,
  last_name varchar(64) NOT NULL,
  password varchar(16) NOT NULL,  
  birthday date NOT NULL,
  minor boolean,
  gender char(1) NOT NULL,
  street_address varchar(128),
  city varchar(64),
  state char(2),  
  zip_code char(5) NOT NULL,
  latitude double,
  longitude double,
  number smallint,
  nickname varchar(16),
  picture varchar(256),
  emergency_contact_name varchar(128),
  emergency_contact_phone char(16),
  referral_code char(6) NOT NULL UNIQUE,
  referred_by integer, 
  created timestamp NOT NULL CONSTRAINT df_players_created DEFAULT now(),
  deleted boolean NOT NULL CONSTRAINT df_players_deleted DEFAULT false,  
  CONSTRAINT fk_players_referred_by FOREIGN KEY (referred_by) REFERENCES players(id)  
);

-- Player email addresses e.g. Keith Donald home, Keith Donald work.
CREATE TABLE player_emails (player integer,
  email varchar(320) NOT NULL UNIQUE,
  label varchar(64) NOT NULL,
  primary_email boolean,
  CONSTRAINT pk_player_emails PRIMARY KEY (player, email),
  CONSTRAINT fk_player_emails_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Player phone numbers e.g. Keith Donald home, Keith Donald mobile
CREATE TABLE player_phones (player integer,
  number varchar(320) NOT NULL UNIQUE,
  label varchar(64) NOT NULL,
  primary_phone boolean,
  CONSTRAINT pk_player_phones PRIMARY KEY (player, number),
  CONSTRAINT fk_player_phones_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Sports players play e.g. Keith Donald plays flag football, softball, basketball, and volleyball.
CREATE TABLE player_sports (player integer,
  sport integer,
  rank smallint,
  level smallint, -- 1-5 self-assessment of skill, 5 being the highest
  CONSTRAINT pk_player_sports PRIMARY KEY (player, sport),
  CONSTRAINT fk_player_sports_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Player children e.g. Keith Donald's children are Annabelle, Corgan, and Juliet.
CREATE TABLE player_children (player integer,
  child integer,
  CONSTRAINT pk_player_child PRIMARY KEY (player, child),
  CONSTRAINT fk_player_children_player FOREIGN KEY (player) REFERENCES players(id),  
  CONSTRAINT fk_player_children_child FOREIGN KEY (child) REFERENCES players(id)
);

-- Pastime Franchises e.g. Hitmen
CREATE TABLE teams (id serial CONSTRAINT pk_teams PRIMARY KEY,
  name varchar(64) NOT NULL,
  founded date,
  founded_by integer,
  logo varchar(256),
  captain integer NOT NULL,
  sport integer NOT NULL,
  created timestamp NOT NULL CONSTRAINT df_teams_created DEFAULT now(),
  created_by integer NOT NULL,
  CONSTRAINT fk_teams_founded_by FOREIGN KEY (founded_by) REFERENCES players(id),
  CONSTRAINT fk_teams_captain FOREIGN KEY (captain) REFERENCES players(id),
  CONSTRAINT fk_teams_sport FOREIGN KEY (sport) REFERENCES sports(id),
  CONSTRAINT fk_teams_created_by FOREIGN KEY (created_by) REFERENCES players(id)  
);

-- Pastime "Franchise" Players e.g. Hitmen Keith Donald, Alexander Weaver, Brian Fisher, etc.
CREATE TABLE team_players (id bigserial CONSTRAINT pk_team_players PRIMARY KEY,
  team integer NOT NULL,
  player integer NOT NULL,
  number smallint,
  nickname varchar(16),
  picture varchar(255),
  CONSTRAINT fk_team_players_team FOREIGN KEY (team) REFERENCES teams(id),
  CONSTRAINT fk_team_players_player FOREIGN KEY (player) REFERENCES players(id)  
);

-- Pastime Organizations e.g. Brevard County Parks and Recreation, Sandox Volleyball
CREATE TABLE organizations (id serial CONSTRAINT pk_organizations PRIMARY KEY,
  name varchar(128) NOT NULL,
  logo varchar(256),
  website varchar(256)
);

-- Venues where games are played e.g. Palm Bay Regional Park, Sands on the Beach
CREATE TABLE venues (id serial CONSTRAINT pk_venues PRIMARY KEY,
  name varchar(128) NOT NULL,
  street_address varchar(128) NOT NULL,
  city varchar(64) NOT NULL,
  state char(2) NOT NULL,
  zip_code char(5) NOT NULL,
  latitude double NOT NULL,  
  longitude double NOT NULL
);

-- Pastime Sponsors e.g. Fired Up Pizza
CREATE TABLE sponsor (id serial CONSTRAINT pk_organizations PRIMARY KEY,
  name varchar(128) NOT NULL,
  logo varchar(256),
  website varchar(256)
);

-- Pastime Leagues e.g. South Brevard Adult Flag Football, Sandbox Open/AA/A/B
CREATE TABLE leagues (id serial CONSTRAINT pk_leagues PRIMARY KEY,
  name varchar(128) NOT NULL,
  sport integer NOT NULL,
  format integer NOT NULL,
  nature char(1), -- (c)ompetitive, (r)ecreational
  gender char(1), -- (m)ale only, (f)emale only, (c)o-ed, null (not specified, accepts either male or female)
  age_min smallint,
  age_max smallint,
  roster_min smallint NOT NULL, -- minimum roster size e.g. 7-on-7 football would be 7.
  roster_healthy smallint NOT NULL, -- size considered healthy (adequate buffer if people can't make it) e.g. 7-on-7 football might be 10.
  roster_max smallint NOT NULL, -- max roster size e.g. 7-on-7 football might be 20.
  roster_min_female smallint,
  skill_level_min smallint,
  skill_level_max smallint,
  registration_type char(1) -- (t)eam, (i)ndividual
  registration_fee money,
  registration_fee_earlybird money,  
  organization integer NOT NULL,
  created timestamp NOT NULL CONSTRAINT df_leagues_created DEFAULT now(),
  CONSTRAINT fk_leagues_sport FOREIGN KEY (sport) REFERENCES sports(id),  
  CONSTRAINT fk_leagues_organization FOREIGN KEY (organization) REFERENCES organizations(id)
);

-- League staff e.g. Commissioner
CREATE TABLE league_staff (league integer,
  player integer,
  role varchar(64) NOT NULL, -- Commissioner, Assistant, Referee
  picture varchar(256),
  CONSTRAINT pk_league_staff PRIMARY KEY (league, player),
  CONSTRAINT fk_league_staff_league FOREIGN KEY (league) REFERENCES leagues(id),
  CONSTRAINT fk_league_staff_player FOREIGN KEY (player) REFERENCES player(id)  
);

-- League to Venue Associations
CREATE TABLE league_venues (league integer,
  venue integer,
  primary_venue boolean,
  CONSTRAINT pk_league_venues PRIMARY KEY (league, venue),
  CONSTRAINT fk_league_venues_league FOREIGN KEY (league) REFERENCES leagues(id),
  CONSTRAINT fk_league_venues_venue FOREIGN KEY (venue) REFERENCES venues(id)  
);

-- Pastime League Seasons e.g. South Brevard Adult Flag Football Winter 2012
CREATE TABLE seasons (id bigserial CONSTRAINT pk_seasons PRIMARY KEY,
  league integer NOT NULL,
  name varchar(64) NOT NULL,
  start_date date,
  registration_opens date,
  registration_closes date,
  status char(1) NOT NULL, -- (n)ew, registration (o)pen, registration (c)losed, (r)unning, x: over
  created timestamp NOT NULL CONSTRAINT df_seasons_created DEFAULT now(),  
  league_name varchar(64) NOT NULL,
  CONSTRAINT fk_seasons_league FOREIGN KEY (league) REFERENCES leagues(id)
);

-- Pastime Free Agents e.g. Efren Blackledge
CREATE TABLE free_agents (season bigint NOT NULL,
  player integer NOT NULL,
  created timestamp NOT NULL CONSTRAINT df_free_agent_created DEFAULT now(),
  message varchar(256),
  CONSTRAINT fk_free_agent_season FOREIGN KEY (season) REFERENCES seasons(id),
  CONSTRAINT fk_free_agent_player FOREIGN KEY (player) REFERENCES players(id),  
);

-- Pastime Team Registrations e.g. Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE registered_teams (id bigserial CONSTRAINT pk_registered_teams PRIMARY KEY,
  team integer NOT NULL,
  season bigint NOT NULL,
  name varchar(64) NOT NULL,
  captain integer NOT NULL,
  status char(1) NOT NULL, -- registration (c)onfirmed, (n)eeds players, (p)ending payment
  created timestamp NOT NULL CONSTRAINT df_registered_team_created DEFAULT now(),
  confirmed timestamp,
  removed boolean,
  CONSTRAINT fk_registered_teams_team FOREIGN KEY (team) REFERENCES teams(id),
  CONSTRAINT fk_registered_teams_season FOREIGN KEY (season) REFERENCES seasons(id),
  CONSTRAINT fk_registered_teams_captain FOREIGN KEY (captain) REFERENCES players(id),
  CONSTRAINT uq_season_team_name UNIQUE (season, name)
);

-- Pastime Team Registration Payments Due e.g. Brian Fisher $480.00; Brian Fisher $25.00, Keith Donald $25.00, etc.
CREATE TABLE registered_team_payments (registered_team bigint NOT NULL,
  player NOT NULL,
  payment_date timestamp,
  amount,
  reference_number,
  created timestamp NOT NULL CONSTRAINT df_registered_team_payments_created DEFAULT now(),  
  CONSTRAINT pk_registered_team_payments PRIMARY KEY (registered_team, player),
  CONSTRAINT fk_registered_team_payments_registered_team FOREIGN KEY (registered_team) REFERENCES registered_teams(id),
  CONSTRAINT fk_registered_team_payments_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Pastime Player Registrations e.g. Keith Donald - Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE registered_players (id bigserial CONSTRAINT pk_registered_players PRIMARY KEY,
  registered_team bigint NOT NULL,
  player integer NOT NULL,
  number smallint NOT NULL,
  nickname varchar(16),
  sub boolean,
  created timestamp NOT NULL CONSTRAINT df_registered_player_created DEFAULT now(),  
  CONSTRAINT fk_registered_players_registered_team FOREIGN KEY (registered_team) REFERENCES registered_teams(id),
  CONSTRAINT fk_registered_players_player FOREIGN KEY (player) REFERENCES players(id)    
);

-- Pastime Team Sponsorship e.g. Fired Up Pizza Hitmen $250.00
CREATE TABLE registered_team_sponsorship (id bigserial CONSTRAINT pk_registered_team_sponsorship PRIMARY KEY,
  registered_team bigint NOT NULL,
  sponsor integer NOT NULL,
  amount money,
  reference_number varchar(16),
  created timestamp NOT NULL CONSTRAINT df_registered_team_sponsorship_created DEFAULT now(),
  CONSTRAINT fk_registered_team_sponsorship_registered_team FOREIGN KEY (registered_team) REFERENCES registered_teams(id),
  CONSTRAINT fk_registered_team_sponsorship_sponsor FOREIGN KEY (sponsor) REFERENCES sponsors(id)    
);

-- Pastime Games e.g. Week 1 - Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE games (id bigserial CONSTRAINT pk_games PRIMARY KEY,
  registered_team bigint NOT NULL,
  number smallint NOT NULL,  
  opponent bigint NOT NULL,
  start_time timestamp NOT NULL,
  CONSTRAINT fk_games_registered_team FOREIGN KEY (registered_team) REFERENCES registered_teams(id),
  CONSTRAINT fk_games_opponent FOREIGN KEY (opponent) REFERENCES registered_teams(id)  
);

-- unique http://pastime.com/{username}
CREATE TABLE usernames (name CONSTRAINT pk_usernames PRIMARY KEY,
  username_type char(1), -- (p)layer, (t)eam, (l)eague
  player_id integer,
  team_id integer,
  league_id integer,
  CONSTRAINT fk_usernames_player_id FOREIGN KEY (player_id) REFERENCES players(id),
  CONSTRAINT fk_usernames_team_id FOREIGN KEY (team_id) REFERENCES teams(id),
  CONSTRAINT fk_usernames_league_id FOREIGN KEY (league_id) REFERENCES leagues(id),  
);

GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO pastime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA public TO pastime;