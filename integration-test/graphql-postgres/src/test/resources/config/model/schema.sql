DROP SCHEMA IF EXISTS db CASCADE;

CREATE SCHEMA db;

CREATE TYPE db.brewery_status AS ENUM ('active', 'inactive');

CREATE TYPE db.taste AS ENUM ('meaty', 'smoky', 'watery', 'fruity', 'spicy');

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
  geometry geometry NOT NULL
);

CREATE TABLE db.beer (
  identifier_beer character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery character varying NOT NULL REFERENCES db.brewery (identifier_brewery),
  sold_per_year INT NOT NULL,
  taste db.taste[] NULL
);

CREATE TABLE db.ingredient (
  identifier_ingredient character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  weight NUMERIC(3,1) NOT NULL
);

CREATE TABLE db.beer_ingredient (
  beer_identifier character varying NOT NULL REFERENCES db.beer (identifier_beer),
  ingredient_identifier character varying NOT NULL REFERENCES db.ingredient (identifier_ingredient),
  PRIMARY KEY (beer_identifier,ingredient_identifier)
);
