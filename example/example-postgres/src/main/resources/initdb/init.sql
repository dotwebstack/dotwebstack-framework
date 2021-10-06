CREATE SCHEMA dbeerpedia;

CREATE EXTENSION IF NOT EXISTS postgis;

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
  record_id BIGINT PRIMARY KEY NOT NULL,
  identifier character varying NOT NULL UNIQUE,
  name character varying NOT NULL,
  status dbeerpedia.brewery_status NOT NULL,
  postal_address character varying REFERENCES dbeerpedia.addresses (identifier),
  visit_address character varying REFERENCES dbeerpedia.addresses (identifier),
  geometry geometry NOT NULL,
  his_age INT NOT NULL,
  his_history character varying NOT NULL
);

INSERT INTO dbeerpedia.breweries(record_id, identifier, name, status, postal_address, visit_address, geometry, his_age, his_history) VALUES
  (1,'d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', ST_Transform(ST_GeometryFromText('POLYGON((5.971385957936759 52.22549347648849,5.972053827981467 52.22549347648849,5.972053827981467 52.225279885758624,5.971385957936759 52.225279885758624,5.971385957936759 52.22549347648849))', 4326),7415), 1988, 'hip and new'),
  (2,'6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL,ST_Transform(ST_GeometryFromText('POLYGON((6.171943938628401 51.48699391333906,6.174325740233626 51.48699391333906,6.174325740233626 51.48564437918149,6.171943938628401 51.48564437918149,6.171943938628401 51.48699391333906))', 4326),7415), 1900, 'A long time ago'),
  (3,'28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL,ST_Transform(ST_GeometryFromText('POLYGON((6.81534412421009 52.207653734352334,6.818358927142829 52.207653734352334,6.818358927142829 52.206457132522104,6.81534412421009 52.206457132522104,6.81534412421009 52.207653734352334))', 4326),7415), 1700, 'A king wanted a spicy beer');

CREATE TABLE dbeerpedia.breweries__related_to (
    brewery_identifier character varying NOT NULL REFERENCES dbeerpedia.breweries (identifier),
    brewery_related_to_identifier character varying NOT NULL REFERENCES dbeerpedia.breweries (identifier),
    PRIMARY KEY (brewery_identifier,brewery_related_to_identifier)
);

INSERT INTO dbeerpedia.breweries__related_to(brewery_identifier, brewery_related_to_identifier) VALUES
    ('d3654375-95fa-46b4-8529-08b0f777bd6b', '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9');

CREATE TABLE dbeerpedia.beers (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery bigint NOT NULL REFERENCES dbeerpedia.breweries (record_id),
  sold_per_year INT NOT NULL,
  taste text[] NOT NULL,
  retired boolean NOT NULL,
  valid_start DATE NOT NULL,
  valid_end DATE,
  available_start timestamp with time zone not null,
  available_end timestamp with time zone
);

CREATE INDEX brewery_idx ON dbeerpedia.beers (brewery);

INSERT INTO dbeerpedia.beers(identifier, name, abv, brewery, sold_per_year, taste, retired, valid_start, valid_end, available_start, available_end) VALUES
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1', 5.4, 1, 1000000, array['MEATY', 'FRUITY']::text[], false,'2020-01-01','2050-01-01','2020-01-01T00:00:00Z',null),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 1, 500000, array['MEATY', 'SPICY', 'SMOKY', 'WATERY', 'FRUITY']::text[], false,'2020-01-01','2020-06-01','2020-01-01T00:00:00Z',null),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, 2, 250000, array['MEATY', 'SMOKY', 'SMOKY']::text[], false,'2020-01-01',null,'2020-01-01T00:00:00Z',null),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 1, 200000, array['SPICY']::text[], false,'2020-01-01',null,'2020-01-01T00:00:00Z',null),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, 2, 100000, array['MEATY', 'SPICY']::text[], false,'2020-01-01',null,'2020-01-01T00:00:00Z','2020-03-01T00:00:00Z'),
  ('766883b5-3482-41cf-a66d-a81e79a4f321', 'Beer 6', 6.5, 2, 25100000, array['WATERY']::text[], true,'2020-01-01',null,'2020-01-01T00:00:00Z',null);


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


CREATE FUNCTION dbeerpedia.breweries_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.breweries AS $$
   SELECT * FROM dbeerpedia.breweries
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.beers_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.beers AS $$
   SELECT * FROM dbeerpedia.beers WHERE daterange(valid_start, valid_end) @> $1 and tstzrange(available_start, available_end) @> $2
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.addresses_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.addresses AS $$
   SELECT * FROM dbeerpedia.addresses
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.ingredients_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.ingredients AS $$
   SELECT * FROM dbeerpedia.ingredients
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.beers_ingredients_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.beers_ingredients AS $$
   SELECT * FROM dbeerpedia.beers_ingredients
$$ language SQL immutable;
