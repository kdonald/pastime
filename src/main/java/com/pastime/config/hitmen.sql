INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('keith@pastimebrevard.com', 'Keith', 'Donald', 9, 'Donald', 'jnhyy5', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('alexander.weaver@gmail.com', 'Alexander', 'Weaver', 37, 'OG Hitman', '96dvlb', 1);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('coolhandluke2222@gmail.com', 'Brian', 'Fisher', 26, 'Fish', 'mvwkdw', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('gabebarfield@gmail.com', 'Gabe', 'Barfield', 87, 'Barefoot', 'kt8mpd', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('tiny28270@gmail.com', 'Marc', 'Szczesny-Pumarada', 21, 'Puma', 'p8cpd9', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('aumematt@gmail.com', 'Matt', 'Wade', 15, 'Dubya', 'in3jrb', null);
INSERT INTO players (email, first_name, last_name, number, nickname, referral_code, referred_by) values ('petrone3000@gmail.com', 'Joe', 'Petrone', 42, 'Petrone', 'xn6z5e', null);

INSERT INTO teams (name, slug, founded, founded_by_name, created_by) values ('Hitmen', 'hitmen', '2000/10/01', 'John Vasquez', 1);
INSERT INTO teams (name, slug, founded, founded_by_name, created_by) values ('Mergers & Acquisitions', 'mergers', null, null, 1);

INSERT INTO team_players (number, nickname, team, player) values (9, 'Donald', 1, 1);
INSERT INTO team_players (number, nickname, team, player) values (37, 'OG Hitman', 1, 2);
INSERT INTO team_players (number, nickname, team, player) values (26, 'Fish', 1, 3);
INSERT INTO team_players (number, nickname, team, player) values (87, 'Barefoot', 1, 4);
INSERT INTO team_players (number, nickname, team, player) values (21, 'Puma', 1, 5);
INSERT INTO team_players (number, nickname, team, player) values (15, 'Dubya', 1, 6);
INSERT INTO team_players (number, nickname, team, player) values (42, 'Petrone', 1, 7);

INSERT INTO leagues (name) values ('South Brevard Adult Flag Football');

INSERT INTO seasons (name, league, start_date) values ('Winter 2012', 1, '2012/01/18');

INSERT INTO registered_teams (team, league, season) values (1, 1, 1);
INSERT INTO registered_teams (team, league, season) values (2, 1, 1);

INSERT INTO registered_players (team, player, number, nickname) values (1, 1, 9, 'Donald');
INSERT INTO registered_players (team, player, number, nickname) values (1, 2, 37, 'OG Hitman');
INSERT INTO registered_players (team, player, number, nickname) values (1, 3, 26, 'Fish');
INSERT INTO registered_players (team, player, number, nickname) values (1, 4, 87, 'Barefoot');
INSERT INTO registered_players (team, player, number, nickname) values (1, 5, 21, 'Puma');
INSERT INTO registered_players (team, player, number, nickname) values (1, 6, 15, 'Wade');
INSERT INTO registered_players (team, player, number, nickname) values (1, 7, 42, 'Joe P.');

INSERT INTO games (team, number, opponent, start_time) values (1, 1, 2, '2012/12/22 6:45:00-05:00');

INSERT INTO next.games (team, number, start_time, opponent) values ('hitmen', 1, '2012/12/22 6:45:00-05:00', 'Mergers & Acquisitions');

INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'donald');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'og-hitman');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'barefoot');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'fish');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'puma');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'dubya');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'petrone');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'berry');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'neil');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'abdoolish');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'slick-p');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'game-changer');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'tomko');
INSERT INTO next.game_attendance (team, game, player) values ('hitmen', 1, 'murray');