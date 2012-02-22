INSERT INTO players (email, first_name, last_name, birthdate, number, nickname, referral_code, referred_by) values ('keith@pastimebrevard.com', 'Keith', 'Donald', '12/29/1977', 9, null, 'jnhyy5', null);
INSERT INTO players (email, first_name, last_name, birthdate, number, nickname, referral_code, referred_by) values ('alexander.weaver@gmail.com', 'Alexander', 'Weaver', '05/20/1977', 37, 'OG Hitman', '96dvlb', 1);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('coolhandluke2222@gmail.com', 'Brian', 'Fisher', 26, 'Fish', 'mvwkdw', null);
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

INSERT INTO next.games (team_slug, number, start_time, opponent, game) values ('hitmen', 1, '2012/12/22 7:45:00-05:00', 'Mergers & Acquisitions', 1);

INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1,'donald', 'Keith', 1);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'og-hitman', 'OG Hitman', 2);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'barefoot', 'Barefoot', 3);
INSERT INTO next.game_attendance (team_slug, game, registered_player_slug, name, player) values ('hitmen', 1, 'fish', 'Fish', 4);
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