CREATE TABLE divisions (
    id bigint auto_increment,
    name varchar(8));

CREATE TABLE teams (
    id bigint auto_increment,
    division_id bigint,
    name varchar(128),
);

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
    away_team_red_cards tinyint
);

CREATE INDEX games_load_index ON games (home_team_id, away_team_id, played_at);
