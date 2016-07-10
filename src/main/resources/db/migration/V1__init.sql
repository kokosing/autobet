CREATE TABLE divisions (
    id bigint auto_increment,
    name varchar(8));

CREATE TABLE teams (
    id bigint auto_increment,
    division_id bigint,
    name varchar(128),
)
