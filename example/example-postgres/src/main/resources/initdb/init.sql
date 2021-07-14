CREATE EXTENSION postgis;
CREATE SCHEMA dbeerpedia;

CREATE TABLE dbeerpedia.addresses (
  identifier character varying NOT NULL PRIMARY KEY,
  street character varying NOT NULL,
  city character varying NOT NULL
);

INSERT INTO dbeerpedia.addresses(identifier, street, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', 'New York');

CREATE TYPE dbeerpedia.brewery_status AS ENUM ('active', 'inactive');

CREATE TABLE dbeerpedia.breweries (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  status dbeerpedia.brewery_status NOT NULL,
  postal_address character varying REFERENCES dbeerpedia.addresses (identifier),
  visit_address character varying REFERENCES dbeerpedia.addresses (identifier),
  geometry geometry NOT NULL,
  his_age INT NOT NULL,
  his_history character varying NOT NULL
);

INSERT INTO dbeerpedia.breweries(identifier, name, status, postal_address, visit_address, geometry, his_age, his_history) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f','POLYGON((5.971385957936759 52.22549347648849,5.972053827981467 52.22549347648849,5.972053827981467 52.225279885758624,5.971385957936759 52.225279885758624,5.971385957936759 52.22549347648849))', 1988, 'hip and new'),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL,'POLYGON((6.171943938628401 51.48699391333906,6.174325740233626 51.48699391333906,6.174325740233626 51.48564437918149,6.171943938628401 51.48564437918149,6.171943938628401 51.48699391333906))', 1900, 'A long time ago'),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL,'POLYGON((6.81534412421009 52.207653734352334,6.818358927142829 52.207653734352334,6.818358927142829 52.206457132522104,6.81534412421009 52.206457132522104,6.81534412421009 52.207653734352334))', 1700, 'A king wanted a spicy beer');

CREATE TABLE dbeerpedia.beers (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery character varying NOT NULL REFERENCES dbeerpedia.breweries (identifier),
  sold_per_year INT NOT NULL,
  taste text[] NOT NULL,
  retired boolean NOT NULL,
  valid_start DATE NOT NULL,
  valid_end DATE
);

CREATE INDEX brewery_idx ON dbeerpedia.beers (brewery);

INSERT INTO dbeerpedia.beers(identifier, name, abv, brewery, sold_per_year, taste, retired, valid_start, valid_end) VALUES
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 1000000, array['MEATY', 'FRUITY']::text[], false,'2020-01-01','2050-01-01'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 500000, array['MEATY', 'SPICY', 'SMOKY', 'WATERY', 'FRUITY']::text[], false,'2020-01-01','2020-06-01'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 250000, array['MEATY', 'SMOKY', 'SMOKY']::text[], false,'2020-01-01',null),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 200000, array['SPICY']::text[], false,'2020-01-01',null),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 100000, array['MEATY', 'SPICY']::text[], false,'2020-01-01',null),
  ('766883b5-3482-41cf-a66d-a81e79a4f321', 'Beer 6', 6.5, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 25100000, array['WATERY']::text[], true,'2020-01-01',null);


CREATE TABLE dbeerpedia.ingredients (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  weight NUMERIC(3,1) NOT NULL
);

INSERT INTO dbeerpedia.ingredients(identifier, name, weight) VALUES
  ('cd795192-5fbb-11eb-ae93-0242ac130002', 'Water', 1.1),
  ('cd794c14-5fbb-11eb-ae93-0242ac130002', 'Hop', 2.2),
  ('cd795196-5fbb-11eb-ae93-0242ac130002', 'Barley', 3.3),
  ('cd795191-5fbb-11eb-ae93-0242ac130002', 'Yeast', 4.4),
  ('cd79538a-5fbb-11eb-ae93-0242ac130002', 'Orange', 5.5),
  ('cd79545c-5fbb-11eb-ae93-0242ac130002', 'Caramel', 6.6);

CREATE TABLE dbeerpedia.beers_ingredients (
  beers_identifier character varying NOT NULL REFERENCES dbeerpedia.beers (identifier),
  ingredients_identifier character varying NOT NULL REFERENCES dbeerpedia.ingredients (identifier),
  PRIMARY KEY (beers_identifier,ingredients_identifier)
);

INSERT INTO dbeerpedia.beers_ingredients(beers_identifier, ingredients_identifier) VALUES
-- beer1
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'cd795192-5fbb-11eb-ae93-0242ac130002'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'cd794c14-5fbb-11eb-ae93-0242ac130002'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'cd795196-5fbb-11eb-ae93-0242ac130002'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'cd795191-5fbb-11eb-ae93-0242ac130002'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'cd79538a-5fbb-11eb-ae93-0242ac130002'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'cd79545c-5fbb-11eb-ae93-0242ac130002'),
-- beer2
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'cd795192-5fbb-11eb-ae93-0242ac130002'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'cd794c14-5fbb-11eb-ae93-0242ac130002'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'cd795196-5fbb-11eb-ae93-0242ac130002'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'cd795191-5fbb-11eb-ae93-0242ac130002'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'cd79538a-5fbb-11eb-ae93-0242ac130002'),
-- beer3
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'cd795192-5fbb-11eb-ae93-0242ac130002'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'cd794c14-5fbb-11eb-ae93-0242ac130002'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'cd795196-5fbb-11eb-ae93-0242ac130002'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'cd795191-5fbb-11eb-ae93-0242ac130002'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'cd79545c-5fbb-11eb-ae93-0242ac130002'),
-- beer4
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'cd795192-5fbb-11eb-ae93-0242ac130002'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'cd794c14-5fbb-11eb-ae93-0242ac130002'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'cd795196-5fbb-11eb-ae93-0242ac130002'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'cd795191-5fbb-11eb-ae93-0242ac130002'),
-- beer5
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'cd795192-5fbb-11eb-ae93-0242ac130002'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'cd794c14-5fbb-11eb-ae93-0242ac130002'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'cd795196-5fbb-11eb-ae93-0242ac130002'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'cd795191-5fbb-11eb-ae93-0242ac130002');


CREATE FUNCTION dbeerpedia.breweries_ctx(date) RETURNS SETOF dbeerpedia.breweries AS $$
   SELECT * FROM dbeerpedia.breweries
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.beers_ctx(date) RETURNS SETOF dbeerpedia.beers AS $$
   SELECT * FROM dbeerpedia.beers WHERE daterange(valid_start, valid_end) @> $1
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.addresses_ctx(date) RETURNS SETOF dbeerpedia.addresses AS $$
   SELECT * FROM dbeerpedia.addresses
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.ingredients_ctx(date) RETURNS SETOF dbeerpedia.ingredients AS $$
   SELECT * FROM dbeerpedia.ingredients
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.beers_ingredients_ctx(date) RETURNS SETOF dbeerpedia.beers_ingredients AS $$
   SELECT * FROM dbeerpedia.beers_ingredients
$$ language SQL immutable;