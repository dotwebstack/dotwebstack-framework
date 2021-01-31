CREATE SCHEMA db;

CREATE TYPE db.brewery_status AS ENUM ('active', 'inactive');

CREATE TABLE db.address (
  identifier character varying NOT NULL PRIMARY KEY,
  street character varying NOT NULL,
  city character varying NOT NULL
);

CREATE TABLE db.brewery (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  status db.brewery_status NOT NULL,
  postal_address character varying REFERENCES db.address (identifier),
  visit_address character varying REFERENCES db.address (identifier)
);

CREATE TABLE db.beer (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery character varying NOT NULL REFERENCES db.brewery (identifier)
);
