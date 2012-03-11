DROP TABLE usernames;
DROP TABLE games;
DROP TABLE team_sponsorship;
DROP TABLE team_member_roles;
DROP TABLE team_members;
DROP TABLE team_payments;
DROP TABLE teams;
DROP TABLE player_pool;
DROP TABLE free_agents;
DROP TABLE seasons;
DROP TABLE league_staff;
DROP TABLE league_venues;
DROP TABLE leagues;
DROP TABLE sponsors;
DROP TABLE venues;
DROP TABLE organizations;
DROP TABLE franchise_member_invites;
DROP TABLE franchise_player_positions;
DROP TABLE franchise_member_roles;
DROP TABLE franchise_members;
DROP TABLE franchises;
DROP TABLE player_children;
DROP TABLE player_sports;
DROP TABLE player_allergies;
DROP TABLE player_medical_conditions;
DROP TABLE player_phones;
DROP TABLE player_emails;
DROP TABLE players;
DROP TABLE format_rules;
DROP TABLE formats;
DROP TABLE sports;

-- Pastime Sports e.g. Flag Football
CREATE TABLE sports (name varchar(64) CONSTRAINT pk_sports PRIMARY KEY,
  logo varchar(256)
);

-- Pastime Sport Formats e.g. 7 on 7 Flag Football
CREATE TABLE formats (sport varchar(64),
  name varchar(64) NOT NULL,
  description varchar(256),
  CONSTRAINT pk_formats PRIMARY KEY (sport, name),
  CONSTRAINT fk_formats_sport FOREIGN KEY (sport) REFERENCES sports(name) ON DELETE CASCADE   
);

-- Pastime Format Rules e.g. Games are 12 minutes long.
CREATE TABLE format_rules (sport varchar(64),
  format varchar(64),
  number smallint,
  title varchar(64),
  description varchar(256),
  CONSTRAINT pk_format_rules PRIMARY KEY (sport, format, number),
  CONSTRAINT fk_format_rules_format FOREIGN KEY (sport, format) REFERENCES formats(sport, name) ON DELETE CASCADE  
);

-- Pastime Players e.g. Keith Donald, Alexander Weaver, Jason Konicki, etc.
CREATE TABLE players (id serial CONSTRAINT pk_players PRIMARY KEY,
  first_name varchar(64) NOT NULL,
  last_name varchar(64) NOT NULL,
  picture varchar(256) NOT NULL,  
  gender char(1) NOT NULL, -- (m)ale, (f)emale  
  birthday date NOT NULL,
  minor boolean,
  height real,
  weight real,
  street_address varchar(128),
  city varchar(64),
  state char(2),  
  zip_code char(5) NOT NULL,
  current_city varchar(128),
  latitude real,
  longitude real,
  hometown varchar(128),
  school varchar(128),
  number smallint,
  nickname varchar(16),
  emergency_contact_name varchar(128),
  emergency_contact_phone char(16),
  vegetarian boolean,
  password varchar(16) NOT NULL,  
  referral_code char(6) NOT NULL UNIQUE,
  referred_by integer, 
  created timestamp NOT NULL CONSTRAINT df_players_created DEFAULT now(),
  deactivated boolean CONSTRAINT df_players_deactivated DEFAULT false,
  CONSTRAINT fk_players_referred_by FOREIGN KEY (referred_by) REFERENCES players(id)  
);

-- Player email addresses e.g. Keith Donald home, Keith Donald work
CREATE TABLE player_emails (email varchar(320),
  label varchar(64) NOT NULL, -- home, work, facebook, etc.
  primary_email boolean NOT NULL CONSTRAINT df_primary_email DEFAULT false,
  player integer,
  CONSTRAINT pk_player_emails PRIMARY KEY (email),
  CONSTRAINT fk_player_emails_player FOREIGN KEY (player) REFERENCES players(id) ON DELETE CASCADE
);

-- Player phone numbers e.g. Keith Donald home, Keith Donald mobile
CREATE TABLE player_phones (number varchar(16),
  label varchar(64) NOT NULL, -- home, work, mobile, etc.
  primary_phone boolean NOT NULL CONSTRAINT df_primary_phone DEFAULT false,
  player integer,  
  CONSTRAINT pk_player_phones PRIMARY KEY (number),
  CONSTRAINT fk_player_phones_player FOREIGN KEY (player) REFERENCES players(id) ON DELETE CASCADE
);

-- Player medical conditions of relevance e.g. asthma, allergies
CREATE TABLE player_medical_conditions (player integer,
  condition varchar(64),
  more_information varchar(256),
  CONSTRAINT pk_player_medical_conditions PRIMARY KEY (player, condition),
  CONSTRAINT fk_player_medical_conditions_player FOREIGN KEY (player) REFERENCES players(id) ON DELETE CASCADE
);

-- Player allergies e.g. peanuts
CREATE TABLE player_allergies (player integer,
  allergy varchar(64),
  more_information varchar(256),
  CONSTRAINT pk_player_allergies PRIMARY KEY (player, allergy),
  CONSTRAINT fk_player_allergies_player FOREIGN KEY (player) REFERENCES players(id) ON DELETE CASCADE
);

-- Sports players play e.g. Keith Donald plays flag football, softball, basketball, and volleyball.
CREATE TABLE player_sports (player integer,
  sport varchar(64),
  rank smallint,
  level smallint, -- 1-5 self-assessment of skill, 5 being the highest
  CONSTRAINT pk_player_sports PRIMARY KEY (player, sport),
  CONSTRAINT fk_player_sports_player FOREIGN KEY (player) REFERENCES players(id) ON DELETE CASCADE,
  CONSTRAINT fk_player_sports_sport FOREIGN KEY (sport) REFERENCES sports(name),
  CONSTRAINT uq_player_sports_rank UNIQUE (player, rank)
);

-- Player children e.g. Keith Donald's children are Annabelle, Corgan, and Juliet.
CREATE TABLE player_children (player integer,
  child integer,
  CONSTRAINT pk_player_child PRIMARY KEY (player, child),
  CONSTRAINT fk_player_children_player FOREIGN KEY (player) REFERENCES players(id),
  CONSTRAINT fk_player_children_child FOREIGN KEY (child) REFERENCES players(id) ON DELETE CASCADE
);

-- Pastime Franchises e.g. Hitmen
CREATE TABLE franchises (id serial CONSTRAINT pk_franchises PRIMARY KEY,
  name varchar(64) NOT NULL,
  sport varchar(64) NOT NULL,
  founded date,
  founder integer,
  logo varchar(256),
  gender char(1), -- (m)ale only, (f)emale only, (c)o-ed, null (not specified, accepts either male or female)
  age_min smallint, -- used to help in adding players (exclude by name search results for people not meeting gender or age_min)
  created timestamp NOT NULL CONSTRAINT df_franchises_created DEFAULT now(),
  CONSTRAINT fk_franchises_sport FOREIGN KEY (sport) REFERENCES sports(name),
  CONSTRAINT fk_franchises_founder FOREIGN KEY (founder) REFERENCES players(id)
);

-- Pastime "Franchise" Members e.g. Hitmen Keith Donald, Alexander Weaver, Brian Fisher, etc.
CREATE TABLE franchise_members (franchise integer,
  player integer,
  number smallint,
  nickname varchar(16),
  picture varchar(256),
  joined date NOT NULL CONSTRAINT df_franchises_joined DEFAULT now(),
  retired date,
  slug varchar(16),  
  CONSTRAINT pk_franchise_members PRIMARY KEY (franchise, player),  
  CONSTRAINT fk_franchise_members_franchise FOREIGN KEY (franchise) REFERENCES franchises(id) ON DELETE CASCADE,
  CONSTRAINT fk_franchise_members_player FOREIGN KEY (player) REFERENCES players(id),
  CONSTRAINT uq_franchise_members_slug UNIQUE (franchise, slug)
);

-- Pastime "Franchise" Member Roles e.g. Keith Donald (Admin, Player); Brian Fisher (Head Coach, Admin, Player).
CREATE TABLE franchise_member_roles (franchise integer,
  player integer,
  role varchar(64), -- Admin, Head Coach, Player, Assistant Coach, etc.
  became date NOT NULL CONSTRAINT df_franchise_member_roles_became DEFAULT now(),
  retired date,
  head_coach_wins smallint,
  head_coach_losses smallint,
  player_captain boolean,
  player_captain_of varchar(64), -- Defense, Offense, Special Teams, etc.
  player_status varchar(1), -- (a)ctive (default), on (l)eave, (i)njured  
  CONSTRAINT pk_franchise_member_roles PRIMARY KEY (franchise, player, role),    
  CONSTRAINT fk_franchise_member_roles_franchise_player FOREIGN KEY (franchise, player) REFERENCES franchise_members(franchise, player) ON DELETE CASCADE
);

-- Pastime "Franchise" Member Invites e.g. Invite for keith.donald@gmail.com to join franchise 'Hitmen'.
CREATE TABLE franchise_member_invites (franchise integer,
  email varchar(320),
  role varchar(64),  
  code varchar(6) NOT NULL UNIQUE,
  sent timestamp NOT NULL CONSTRAINT df_franchise_member_invites_sent DEFAULT now(),
  sent_by integer NOT NULL,
  player integer,  
  accepted boolean,
  accepted_player integer,
  answered_on timestamp,
  CONSTRAINT pk_franchise_member_invites PRIMARY KEY (franchise, email, role),
  CONSTRAINT fk_franchise_member_invites_franchise FOREIGN KEY (franchise) REFERENCES franchises(id) ON DELETE CASCADE,
  CONSTRAINT fk_franchise_member_invites_franchise_sent_by FOREIGN KEY (franchise, sent_by) REFERENCES franchise_members(franchise, player),
  CONSTRAINT fk_franchise_member_invites_player FOREIGN KEY (player) REFERENCES players(id),
  CONSTRAINT fk_franchise_member_invites_accepted_player FOREIGN KEY (accepted_player) REFERENCES players(id)  
);

-- Pastime "Franchise" Player Positions
CREATE TABLE franchise_player_positions (franchise integer,
  player integer,
  position varchar(16), -- 'QB', 'RB', 'WR', 'S', 'LB', 'CB', 'C', 'R', etc.
  rank smallint NOT NULL, -- determines order positions are listed for players who play multiple
  depth smallint, -- basis for positional depth chart e.g. 1 for WR1, 2 for WR2, etc.
  CONSTRAINT pk_franchise_player_positions PRIMARY KEY (franchise, player, position),  
  CONSTRAINT fk_franchise_player_positions_franchise FOREIGN KEY (franchise) REFERENCES franchises(id) ON DELETE CASCADE,
  CONSTRAINT fk_franchise_player_positions_player FOREIGN KEY (player) REFERENCES players(id) 
);

-- Pastime Organizations e.g. Brevard County Parks and Recreation, Sandox Volleyball
CREATE TABLE organizations (id serial CONSTRAINT pk_organizations PRIMARY KEY,
  name varchar(128) NOT NULL,
  logo varchar(256),
  founded date,  
  website varchar(256)
);

-- Venues where games are played e.g. Palm Bay Regional Park, Sands on the Beach
CREATE TABLE venues (id serial CONSTRAINT pk_venues PRIMARY KEY,
  name varchar(128) NOT NULL,
  street_address varchar(128) NOT NULL,
  city varchar(64) NOT NULL,
  state char(2) NOT NULL,
  zip_code char(5) NOT NULL,
  latitude real NOT NULL,  
  longitude real NOT NULL
);

-- Pastime Sponsors e.g. Fired Up Pizza
CREATE TABLE sponsors (id serial CONSTRAINT pk_sponsor PRIMARY KEY,
  name varchar(128) NOT NULL,
  logo varchar(256),
  website varchar(256)
);

-- Pastime Leagues e.g. South Brevard Adult Flag Football, Sandbox Open/AA/A/B
CREATE TABLE leagues (id serial CONSTRAINT pk_leagues PRIMARY KEY,
  name varchar(128) NOT NULL,
  slug varchar(16),  
  sport varchar(64) NOT NULL,
  picture varchar(64) NOT NULL,
  format varchar(64) NOT NULL,
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
  registration_type char(1), -- (t)eam, (i)ndividual
  registration_fee money,
  registration_fee_earlybird money,  
  organization integer NOT NULL,
  created timestamp NOT NULL CONSTRAINT df_leagues_created DEFAULT now(),
  CONSTRAINT fk_leagues_sport FOREIGN KEY (sport) REFERENCES sports(name),
  CONSTRAINT fk_leagues_format FOREIGN KEY (sport, format) REFERENCES formats(sport, name),  
  CONSTRAINT fk_leagues_organization FOREIGN KEY (organization) REFERENCES organizations(id),
  CONSTRAINT uq_leagues_organization_league_name UNIQUE (organization, name),
  CONSTRAINT uq_leagues_organization_league_slug UNIQUE (organization, slug)  
);

-- League staff e.g. Commissioner
CREATE TABLE league_staff (league integer,
  player integer,
  role varchar(64) NOT NULL, -- Commissioner, Assistant, Referee, Admin
  picture varchar(256),
  status char(1), -- (a)ctive, (r)etired
  joined date,
  retired date,
  CONSTRAINT pk_league_staff PRIMARY KEY (league, player),
  CONSTRAINT fk_league_staff_league FOREIGN KEY (league) REFERENCES leagues(id) ON DELETE CASCADE,
  CONSTRAINT fk_league_staff_player FOREIGN KEY (player) REFERENCES players(id)  
);

-- League to Venue Associations
CREATE TABLE league_venues (league integer,
  venue integer,
  primary_venue boolean,
  CONSTRAINT pk_league_venues PRIMARY KEY (league, venue),
  CONSTRAINT fk_league_venues_league FOREIGN KEY (league) REFERENCES leagues(id) ON DELETE CASCADE,
  CONSTRAINT fk_league_venues_venue FOREIGN KEY (venue) REFERENCES venues(id)
);

-- Pastime League Seasons e.g. South Brevard Adult Flag Football Winter 2012
CREATE TABLE seasons (league integer,
  number integer,
  name varchar(64) NOT NULL, -- for history since league name can change
  picture varchar(256) NOT NULL, -- for history since league picture can change
  start_date date,
  registration_opens date,
  registration_closes date,
  status char(1) NOT NULL, -- (n)ew, registration (o)pen, registration (c)losed, (r)unning, (x) over
  created timestamp NOT NULL CONSTRAINT df_seasons_created DEFAULT now(),  
  CONSTRAINT pk_seasons PRIMARY KEY (league, number),  
  CONSTRAINT fk_seasons_league FOREIGN KEY (league) REFERENCES leagues(id)
);

-- Pastime Free Agents e.g. Efren Blackledge
CREATE TABLE free_agents (league integer,
  season integer,
  player integer,
  created timestamp NOT NULL CONSTRAINT df_free_agent_created DEFAULT now(),
  message varchar(256),
  CONSTRAINT pk_free_agents PRIMARY KEY (league, season, player),  
  CONSTRAINT fk_free_agent_season FOREIGN KEY (league, season) REFERENCES seasons(league, number),
  CONSTRAINT fk_free_agent_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Pool of registered individual players, serves as input into a draft. Individual registration leagues only.
CREATE TABLE player_pool (league integer,
  season integer,
  player integer,
  status char(1) NOT NULL, -- registration (c)onfirmed, (p)ending payment
  created timestamp NOT NULL CONSTRAINT df_player_pool_created DEFAULT now(),
  confirmed timestamp, 
  removed boolean,
  CONSTRAINT pk_player_pool PRIMARY KEY (league, season, player),
  CONSTRAINT fk_player_pool_season FOREIGN KEY (league, season) REFERENCES seasons(league, number),
  CONSTRAINT fk_player_pool_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Pastime Team Registrations e.g. Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE teams (league integer,
  season integer,
  team integer,
  name varchar(64) NOT NULL,
  logo varchar(256),
  status char(1) NOT NULL, -- registration (c)onfirmed, (n)eeds players, (p)ending payment
  confirmed timestamp,
  created timestamp NOT NULL CONSTRAINT df_teams_created DEFAULT now(),  
  removed boolean,
  franchise integer,  
  CONSTRAINT pk_teams PRIMARY KEY (league, season, team),
  CONSTRAINT fk_teams_season FOREIGN KEY (league, season) REFERENCES seasons(league, number),  
  CONSTRAINT fk_teams_franchise FOREIGN KEY (franchise) REFERENCES franchises(id),
  CONSTRAINT uq_teams_team_name UNIQUE (league, season, name)
);

-- Pastime Team Registration Payments Due e.g. Brian Fisher $480.00; Brian Fisher $25.00, Keith Donald $25.00, etc.
CREATE TABLE team_payments (league integer,
  season integer,
  team integer,
  player integer,
  payment_date timestamp,
  amount money,
  reference_number varchar(16),
  created timestamp NOT NULL CONSTRAINT df_team_payments_created DEFAULT now(),  
  CONSTRAINT pk_team_payments PRIMARY KEY (league, season, team, player),
  CONSTRAINT fk_team_payments_team FOREIGN KEY (league, season, team) REFERENCES teams(league, season, team),
  CONSTRAINT fk_team_payments_player FOREIGN KEY (player) REFERENCES players(id)
);

-- Pastime Team Member Registrations e.g. Keith Donald - Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE team_members (league integer,
  season integer,
  team integer,  
  player integer,
  number smallint NOT NULL, -- for history since franchise number can change
  nickname varchar(16) NOT NULL, -- name on jersey; for history since franchise nickname can change
  picture varchar(256) NOT NULL, -- for history since franchise picture can change
  created timestamp NOT NULL CONSTRAINT df_team_members_created DEFAULT now(),
  CONSTRAINT pk_team_members PRIMARY KEY (league, season, team, player),
  CONSTRAINT fk_team_members_team FOREIGN KEY (league, season, team) REFERENCES teams(league, season, team),
  CONSTRAINT fk_team_members_player FOREIGN KEY (player) REFERENCES players(id),
  CONSTRAINT uq_team_members_number UNIQUE (league, season, team, number),
  CONSTRAINT uq_team_members_nickname UNIQUE (league, season, team, nickname)  
);

-- Pastime Registered Team Member Roles e.g. Keith Donald (Player); Brian Fisher (Head Coach, Player).
CREATE TABLE team_member_roles (league integer,
  season integer,
  team integer,
  player integer,
  role varchar(64), -- Admin, Head Coach, Assistant Coach, Player, etc.
  player_captain boolean,
  player_captain_of varchar(64), -- Defense, Offense, Special Teams, etc.
  player_status varchar(1), -- (a)ctive (default), on (l)eave, (i)njured  
  player_sub boolean, -- sub's don't usually pay and are only included on reminders when team is short players
  CONSTRAINT pk_team_member_roles PRIMARY KEY (league, season, team, player, role),    
  CONSTRAINT fk_team_member_roles_team_member FOREIGN KEY (league, season, team, player) REFERENCES team_members(league, season, team, player) ON DELETE CASCADE
);

-- Pastime Team Sponsorship e.g. Fired Up Pizza Hitmen $250.00
CREATE TABLE team_sponsorship (league integer,
  season integer,
  team integer,
  sponsor integer,
  amount money,
  reference_number varchar(16),
  created timestamp NOT NULL CONSTRAINT df_team_sponsorship_created DEFAULT now(),
  CONSTRAINT pk_team_sponsorship PRIMARY KEY (league, season, team, sponsor),
  CONSTRAINT fk_team_sponsorship_team FOREIGN KEY (league, season, team) REFERENCES teams(league, season, team),
  CONSTRAINT fk_team_sponsorship_sponsor FOREIGN KEY (sponsor) REFERENCES sponsors(id)    
);

-- Pastime Games e.g. Week 1 - Hitmen - South Brevard Adult Flag Football Winter 2012
CREATE TABLE games (league integer,
  season integer,
  team integer,
  number smallint,  
  opponent bigint NOT NULL,
  start_time timestamp NOT NULL,
  CONSTRAINT pk_games PRIMARY KEY (league, season, team, number),
  CONSTRAINT fk_games_team FOREIGN KEY (league, season, team) REFERENCES teams(league, season, team),
  CONSTRAINT fk_games_opponent FOREIGN KEY (league, season, opponent) REFERENCES teams(league, season, team)  
);

-- unique http://pastime.com/{username}
CREATE TABLE usernames (username varchar(16) CONSTRAINT pk_usernames PRIMARY KEY,
  username_type char(1), -- (p)layer, (f)ranchise, league (o)rganization
  player integer,
  franchise integer,
  organization integer,
  CONSTRAINT fk_usernames_player FOREIGN KEY (player) REFERENCES players(id),
  CONSTRAINT fk_usernames_franchise FOREIGN KEY (franchise) REFERENCES franchises(id),
  CONSTRAINT fk_usernames_organization FOREIGN KEY (organization) REFERENCES organizations(id)
);

GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO pastime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA public TO pastime;