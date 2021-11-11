CREATE SCHEMA db;

CREATE EXTENSION IF NOT EXISTS postgis;

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
  geometry geometry(GeometryZ, 7415) NOT NULL,
  geometry_7931 geometry(GeometryZ, 7931) NOT NULL,
  his_age INT NOT NULL,
  his_history character varying NOT NULL,
  multinational boolean NOT NULL
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
