CREATE TABLE divisions (
    id bigint auto_increment,
    name varchar(8),
    primary key (id));

CREATE TABLE teams (
    id bigint auto_increment,
    division_id bigint,
    name varchar(128),
    primary key (id));

CREATE TABLE games (
    id bigint auto_increment,
    home_team_id bigint,
    away_team_id bigint,
    played_at date,

    -- score
    full_time_home_team_goals tinyint,
    full_time_away_team_goals tinyint,
    full_time_result varchar(1),
    half_time_home_team_goals tinyint,
    half_time_away_team_goals tinyint,
    half_time_result varchar(1),

    -- stats
    attendance bigint,
    referee varchar(100),
    home_team_shots tinyint,
    away_team_shots tinyint,
    home_team_shots_on_target tinyint,
    away_team_shots_on_target tinyint,
    home_team_hit_woodwork tinyint,
    away_team_hit_woodwork tinyint,
    home_team_corners tinyint,
    away_team_corners tinyint,
    home_team_fouls_committed tinyint,
    away_team_fouls_committed tinyint,
    home_team_offsides tinyint,
    away_team_offsides tinyint,
    home_team_yellow_cards tinyint,
    away_team_yellow_cards tinyint,
    home_team_red_cards tinyint,
    away_team_red_cards tinyint,
    primary key (id));

CREATE INDEX games_load_index ON games (home_team_id, away_team_id, played_at);

CREATE TABLE bet_types (
    id bigint auto_increment,
    name varchar(128),
    expression varchar(2048),
    bet_suffix varchar(4),
    primary key (id));

INSERT INTO bet_types VALUES
    (1, 'full_time_home_win', 'full_time_home_team_goals > full_time_away_team_goals', 'H'),
    (2, 'full_time_away_win', 'full_time_home_team_goals < full_time_away_team_goals', 'A'),
    (3, 'full_time_draw',     'full_time_home_team_goals = full_time_away_team_goals', 'D');

CREATE TABLE bet_vendors (
    id bigint auto_increment,
    name varchar(128),
    bet_prefix varchar(16),
    primary key (id));

INSERT INTO bet_vendors VALUES
    (1, 'Bet365', 'B365'),
    (2, 'Blue Square', 'BS'),
    (3, 'Bet&Win', 'BW'),
    (4, 'Gamebookers', 'GB'),
    (5, 'Interwetten', 'IW'),
    (6, 'Ladbrokes', 'LB'),
    (7, 'Pinnacle Sports', 'PS'),
    (8, 'Sporting Odds', 'SO'),
    (9, 'Sportingbet', 'SB'),
    (10, 'Stan James', 'SJ'),
    (11, 'Stanleybet', 'SY'),
    (12, 'VC Bet', 'VC'),
    (13, 'William Hill', 'WH');

CREATE TABLE bets (
    id bigint auto_increment,
    bet_vendor_id bigint,
    bet_type_id bigint,
    game_id bigint,
    odds decimal(10,4),
    primary key (id));

CREATE INDEX bets_by_game_index ON bets (game_id);

CREATE TABLE key_value_store_entries (
    id bigint auto_increment,
    created_at timestamp,
    _key varchar(128),
    _value text,
    primary key (id));
