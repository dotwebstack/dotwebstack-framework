CREATE SCHEMA db;

CREATE TYPE db.brewery_status AS ENUM ('active', 'inactive');

CREATE TYPE db.taste AS ENUM ('MEATY', 'SMOKY', 'WATERY', 'FRUITY', 'SPICY');

CREATE TABLE db.address_i (
  identifier_address character varying PRIMARY KEY
);

CREATE TABLE db.address_v (
  record_id SERIAL,
  identifier_address character varying REFERENCES db.address_i (identifier_address),
  street character varying NOT NULL,
  city character varying NOT NULL,
  CONSTRAINT address_pkey PRIMARY KEY (record_id)
);

CREATE TABLE db.brewery_i (
  identifier_brewery character varying PRIMARY KEY
);

CREATE TABLE db.brewery_v (
  record_id SERIAL,
  identifier_brewery character varying REFERENCES db.brewery_i (identifier_brewery),
  name character varying NOT NULL,
  status db.brewery_status NOT NULL,
  postal_address character varying REFERENCES db.address_i (identifier_address),
  visit_address character varying REFERENCES db.address_i (identifier_address),
  geometry geometry NOT NULL,
  his_age INT NOT NULL,
  his_history character varying NOT NULL,
  multinational boolean NOT NULL,
  valid_start DATE NOT NULL,
  valid_end DATE,
  available_start timestamp with time zone not null,
  available_end timestamp with time zone,
  CONSTRAINT brewery_pkey PRIMARY KEY (record_id)
);

CREATE TABLE db.beer_i (
  identifier_beer character varying PRIMARY KEY
);

CREATE TABLE db.beer_v (
  record_id SERIAL,
  identifier_beer character varying NOT NULL REFERENCES db.beer_i (identifier_beer),
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery character varying NOT NULL REFERENCES db.brewery_i (identifier_brewery),
  sold_per_year INT NOT NULL,
  taste db.taste[] NULL,
  since date NOT NULL,
  last_brewed timestamp NOT NULL,
  valid_start DATE NOT NULL,
  valid_end DATE,
  available_start timestamp with time zone not null,
  available_end timestamp with time zone,
  CONSTRAINT beer_pkey PRIMARY KEY (record_id)
);

CREATE FUNCTION db.brewery_v_ctx(date,timestamp with time zone) RETURNS SETOF db.brewery_v AS $$
   SELECT * FROM db.brewery_v WHERE daterange(valid_start, valid_end) @> $1 and tstzrange(available_start, available_end) @> $2
$$ language SQL immutable;

CREATE FUNCTION db.beer_v_ctx(date,timestamp with time zone) RETURNS SETOF db.beer_v AS $$
   SELECT * FROM db.beer_v WHERE daterange(valid_start, valid_end) @> $1 and tstzrange(available_start, available_end) @> $2
$$ language SQL immutable;

CREATE FUNCTION db.address_v_ctx(date,timestamp with time zone) RETURNS SETOF db.address_v AS $$
   SELECT * FROM db.address_v
$$ language SQL immutable;