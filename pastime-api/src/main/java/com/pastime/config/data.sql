INSERT INTO sports (name) VALUES ('Flag Football');
INSERT INTO formats (name, sport) VALUES ('7 on 7', 'Flag Football');

INSERT INTO sports (name) VALUES ('Volleyball');
INSERT INTO formats (name, sport) VALUES ('6 on 6', 'Volleyball');

INSERT INTO organizations (id, name, founded, website) VALUES (1, 'Brevard County Parks and Recreation', null, 'http://www.brevardparks.com');
INSERT INTO usernames (username, username_type, organization) VALUES ('brevardparks', 'o', 1);

INSERT INTO venues (id, name, street_address, city, state, zip_code, latitude, longitude)
  VALUES (1, 'Palm Bay Regional Park', '951 Malabar Rd. NW', 'Palm Bay', 'FL', '32908', 28.005094974728667, -80.73124302540815);

INSERT INTO venues (id, name, street_address, city, state, zip_code, latitude, longitude)
  VALUES (2, 'Max Rodes Park', '3410 Flanagan Avenue', 'West Melbourne', 'FL', '32904', 28.05723, -80.680772);

-- Flag Football

INSERT INTO leagues (id, name, slug, sport, format, nature, gender, age_min, age_max,
  roster_min, roster_healthy, roster_max, roster_min_female, skill_level_min, skill_level_max, registration_type, registration_fee, registration_fee_earlybird, organization)
    VALUES (1, 'South Brevard Adult Flag Football', 'south-flag', 'Flag Football', '7 on 7', null, null, 17, null, 7, 10, 20, null, null, null, 't', '$480.00', null, 1);

INSERT INTO league_venues (league, venue, primary_venue) VALUES (1, 1, true);

INSERT INTO seasons (league, number, name, slug, registration_status) VALUES (1, 1, 'South Brevard Adult Flag Football', '2012-spring', 'o');
 
-- Volleyball

INSERT INTO leagues (id, name, slug, sport, format, nature, gender, age_min, age_max,
  roster_min, roster_healthy, roster_max, roster_min_female, skill_level_min, skill_level_max, registration_type, registration_fee, registration_fee_earlybird, organization)
    VALUES (2, 'Max Rodes Indoor Volleyball', 'south-indoor', 'Volleyball', '6 on 6', null, 'c', 17, null, 6, 6, 20, 2, null, null, 't', '$500.00', null, 1);

INSERT INTO league_venues (league, venue, primary_venue) VALUES (2, 2, true);

INSERT INTO seasons (league, number, name, slug) VALUES (2, 1, 'Max Rodes Indoor Volleyball', '2012-spring');

  
-- Teams and Players

INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, number, nickname, password, referral_code, referred_by)
  VALUES (1, 'Keith', 'Donald', 'm', '1977-12-29', '32904', 9, 'Donald', 'whippet', 'jnhyy5', null);
INSERT INTO player_emails (player, email, primary_email) VALUES (1, 'keith.donald@gmail.com', true);

INSERT INTO players (id, first_name, last_name, gender, birthday, zip_code, number, nickname, password, referral_code, referred_by)
  VALUES (2, 'Alexander', 'Weaver', 'm', '1978-05-20', '32905', 37, 'OG Hitman', 'whippet', '96dvlb', null);
INSERT INTO player_emails (player, email, primary_email) VALUES (2, 'keith@pastime.com', true);

INSERT INTO players (first_name, last_name, gender, birthday, zip_code, number, nickname, password, referral_code, referred_by)
  VALUES ('Brian', 'Fisher', 'm', '1970-10-20', '32904', 26, 'Fish', 'whippet', 'mvwkdw', null);
INSERT INTO player_emails (player, email, primary_email) VALUES (3, 'coolhandluke2222@gmail.com', true);

INSERT INTO teams (league, season, team, name, status) VALUES (1, 1, 1, 'Hitmen', 'n');

INSERT INTO team_members (league, season, team, player, slug, number, nickname) VALUES (1, 1, 1, 1, 'donald', 9, 'Donald');
INSERT INTO team_member_roles (league, season, team, player, role, player_captain, player_sub, player_status) VALUES (1, 1, 1, 1, 'Player', false, false, 'a');
  
INSERT INTO team_members (league, season, team, player, slug, number, nickname) VALUES (1, 1, 1, 2, 'og-hitman', 37, 'OG Hitman');
INSERT INTO team_member_roles (league, season, team, player, role, player_captain, player_captain_of, player_sub, player_status) VALUES (1, 1, 1, 2, 'Player', true, 'Defense', false, 'a');

INSERT INTO team_members (league, season, team, player, slug, number, nickname) VALUES (1, 1, 1, 3, 'fish', 26, 'Fish');
INSERT INTO team_member_roles (league, season, team, player, role) VALUES (1, 1, 1, 3, 'Head Coach');  
INSERT INTO team_member_roles (league, season, team, player, role, player_captain, player_sub, player_status) VALUES (1, 1, 1, 3, 'Player', false, false, 'i');


  
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('gabebarfield@gmail.com', 'Gabe', 'Barfield', 87, 'Barefoot', 'kt8mpd', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('tiny28270@gmail.com', 'Marc', 'Szczesny-Pumarada', 21, 'Puma', 'p8cpd9', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('aumematt@gmail.com', 'Matt', 'Wade', 15, 'Dubya', 'in3jrb', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('petrone3000@gmail.com', 'Joe', 'Petrone', 42, null, 'xn6z5e', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('jasona.berry@regions.com', 'Jason', 'Berry', 11, null, '4eys8c', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('431larochelle@gmail.com', 'Neil', 'Larochelle', 4, null, 'dxd6sn', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('nunez.aldo@yahoo.com', 'Aldo', 'Nunez', 13, 'Abdoolish', 'zuvx2d', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('slickp32@gmail.com', 'Dale', 'Haines', 22, 'Slick P', 'jrbji2', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('keith.donald@gmail.com', 'Dez', 'Carver', 23, 'Game Changer', 'zrxmvn', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('stomko@harris.com', 'Steve', 'Tomko', 99, null, 'clnyz5', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('dmurra05@harris.com', 'Dave', 'Murray', 7, null, 'wrg8co', null);

INSERT INTO teams (name, slug, founded, founded_by_name, created_by, captain) values ('Hitmen', 'hitmen', '2000/10/01', 'John Vasquez', 1, 3);
INSERT INTO teams (name, slug, founded, founded_by_name, created_by) values ('Mergers & Acquisitions', 'mergers', null, null, 1);

INSERT INTO team_players (team, player, number, nickname) values (1, 1, 9, null);
INSERT INTO team_players (team, player, number, nickname) values (1, 2, 37, 'OG Hitman');
INSERT INTO team_players (team, player, number, nickname) values (1, 3, 26, 'Fish');
INSERT INTO team_players (team, player, number, nickname) values (1, 4, 87, 'Barefoot');
INSERT INTO team_players (team, player, number, nickname) values (1, 5, 21, 'Puma');
INSERT INTO team_players (team, player, number, nickname) values (1, 6, 15, 'Dubya');
INSERT INTO team_players (team, player, number, nickname) values (1, 7, 42, null);
INSERT INTO team_players (team, player, number, nickname) values (1, 8, 11, null);
INSERT INTO team_players (team, player, number, nickname) values (1, 9, 4, nul);
INSERT INTO team_players (team, player, number, nickname) values (1, 10, 13, 'Abdoolish');
INSERT INTO team_players (team, player, number, nickname) values (1, 11, 22, 'Slick P');
INSERT INTO team_players (team, player, number, nickname) values (1, 12, 23, 'Game Changer');
INSERT INTO team_players (team, player, number, nickname) values (1, 13, 99, null);
INSERT INTO team_players (team, player, number, nickname) values (1, 14, 7, null);

INSERT INTO leagues (name, sport) values ('South Brevard Adult Flag Football', 'Flag Football');

INSERT INTO seasons (league, name, start_date, league_name) values (1, 'Winter 2012', '2012/01/18', 'South Brevard Adult Flag Football');

INSERT INTO registered_teams (team, season, name, captain) values (1, 1, 'Hitmen', 3);
INSERT INTO registered_teams (team, season, name, captain) values (2, 1, 'Mergers & Acquisitions', 1);

INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 1, 9, null);
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 2, 37, 'OG Hitman');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 3, 26, 'Fish');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 4, 87, 'Barefoot');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 5, 21, 'Puma');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 6, 15, 'Dubya');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 7, 42, null);
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 8, 11, null);
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 9, 4, null);
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 10, 13, 'Abdoolish');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 11, 22, 'Slick P');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 12, 23, 'Game Changer');
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 13, 99, null);
INSERT INTO registered_players (registered_team, player, number, nickname) values (1, 14, 7, null);

INSERT INTO games (registered_team, number, opponent, start_time) values (1, 1, 2, '2012/12/22 7:45:00-05:00');

INSERT INTO next.games (team_slug, number, start_time, opponent, game) values ('hitmen', 1, '2012/12/22 19:45:00-05:00', 'Mergers & Acquisitions', 1);

INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1,'donald', 'Keith', 1);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'og-hitman', 'OG Hitman', 2);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'fish', 'Fish', 3);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'barefoot', 'Barefoot', 4);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'puma', 'Puma', 5);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'dubya', 'Dubya', 6);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'petrone', 'Joe', 7);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'berry', 'Jason', 8);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'neil', 'Neil', 9);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'abdoolish', 'Abdoolish', 10);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'slick-p', 'Slick P', 11);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'game-changer', 'Game Changer', 12);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'tomko', 'Steve', 13);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'murray', 'Dave', 14);