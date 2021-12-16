CREATE SCHEMA db;

CREATE TYPE db.brewery_status AS ENUM ('active', 'inactive');

CREATE TYPE db.taste AS ENUM ('MEATY', 'SMOKY', 'WATERY', 'FRUITY', 'SPICY');

CREATE TABLE db.address (
  identifier_address character varying NOT NULL PRIMARY KEY,
  street character varying NOT NULL,
  city character varying NOT NULL
);

CREATE TABLE db.brewery (
  identifier_brewery character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  status db.brewery_status NOT NULL,
  postal_address character varying REFERENCES db.address (identifier_address),
  visit_address character varying REFERENCES db.address (identifier_address),
  geometry geometry(Geometry, 4326) NOT NULL,
  his_age INT NOT NULL,
  his_history character varying NOT NULL,
  multinational boolean NOT NULL,
  valid_start DATE NOT NULL,
  valid_end DATE,
  available_start timestamp with time zone not null,
  available_end timestamp with time zone
);

CREATE TABLE db.beer (
  identifier_beer character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery character varying NOT NULL REFERENCES db.brewery (identifier_brewery),
  sold_per_year INT NOT NULL,
  taste db.taste[] NULL,
  since date NOT NULL,
  last_brewed timestamp NOT NULL
);

CREATE TABLE db.ingredient (
  identifier_ingredient character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  code character varying(4) NOT NULL UNIQUE,
  weight NUMERIC(3,1) NOT NULL
);

CREATE TABLE db.beer_ingredient (
  beer_identifier character varying NOT NULL REFERENCES db.beer (identifier_beer),
  ingredient_code character varying(4) NOT NULL REFERENCES db.ingredient (code),
  PRIMARY KEY (beer_identifier,ingredient_code)
);

CREATE FUNCTION db.brewery_history_ctx(date,timestamp with time zone) RETURNS SETOF db.brewery AS $$
   SELECT * FROM db.brewery WHERE daterange(valid_start, valid_end) @> $1 and tstzrange(available_start, available_end) @> $2
$$ language SQL immutable;

CREATE FUNCTION db.beer_history_ctx(date,timestamp with time zone) RETURNS SETOF db.beer AS $$
   SELECT * FROM db.beer
$$ language SQL immutable;

CREATE FUNCTION db.address_history_ctx(date,timestamp with time zone) RETURNS SETOF db.address AS $$
   SELECT * FROM db.address
$$ language SQL immutable;

CREATE FUNCTION db.ingredient_history_ctx(date,timestamp with time zone) RETURNS SETOF db.ingredient AS $$
   SELECT * FROM db.ingredient
$$ language SQL immutable;

CREATE FUNCTION db.beer_ingredient_history_ctx(date,timestamp with time zone) RETURNS SETOF db.beer_ingredient AS $$
   SELECT * FROM db.beer_ingredient
$$ language SQL immutable;

