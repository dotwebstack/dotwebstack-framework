CREATE EXTENSION postgis;
CREATE SCHEMA dbeerpedia;

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE dbeerpedia.addresses (
  identifier character varying NOT NULL PRIMARY KEY,
  street character varying NOT NULL,
  street_tsv TSVECTOR NOT NULL,
  city character varying NOT NULL
);

INSERT INTO dbeerpedia.addresses(identifier, street, street_tsv, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', to_tsvector('Ch Chu Chur Churc Church St Str Stre Stree Street'), 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', to_tsvector('5t 5th Av Ave Aven Avenu Avenue'), 'New York');

CREATE TYPE dbeerpedia.brewery_status AS ENUM ('active', 'inactive');

CREATE TABLE dbeerpedia.breweries (
  record_id BIGINT PRIMARY KEY NOT NULL,
  identifier character varying NOT NULL UNIQUE,
  name character varying NOT NULL,
  status dbeerpedia.brewery_status NOT NULL,
  postal_address character varying REFERENCES dbeerpedia.addresses (identifier),
  visit_address character varying REFERENCES dbeerpedia.addresses (identifier),
  geometry geometry(GeometryZ, 7415) NOT NULL,
  geometry_bbox geometry(Geometry, 7415) NOT NULL,
  geometry_etrs89 geometry(GeometryZ, 7931) NOT NULL,
  his boolean not null,
  his_age INT,
  his_history character varying
);

INSERT INTO dbeerpedia.breweries(record_id, identifier, name, status, postal_address, visit_address, geometry, geometry_bbox, geometry_etrs89, his, his_age, his_history) VALUES
  (1,'d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', ST_GeometryFromText('POLYGON Z((206410.1605 447480.1649 3, 206412.3799 447474.7692 3, 206418.7599 447476.6259 3, 206417.4787 447480.3322 3, 206423.1208 447482.3191 3, 206423.0706 447482.7319 3, 206416.4167 447480.6427 3, 206415.9896 447481.8782 3, 206410.1605 447480.1649 3))', 7415), ST_GeometryFromText('POLYGON((206410.1605 447474.7692, 206410.1605 447482.7319, 206423.1208 447482.7319, 206423.1208 447474.7692, 206410.1605 447474.7692))', 7415), ST_GeometryFromText('POLYGON Z((6.136068105697632 52.01329602598457 1.123, 6.136099621653557 52.01324732611223 1.123, 6.136192828416824 52.013263421838616 1.123, 6.136174723505974 52.013296851405684 1.123, 6.136257201433181 52.01331418524545 1.123, 6.136256530880928 52.01331789963881 1.123, 6.136159300804138 52.01329974037945 1.123, 6.136153265833855 52.01331088356219 1.123, 6.136068105697632 52.01329602598457 1.123))', 7931), true, 1988, 'hip and new'),
  (2,'6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL, ST_GeometryFromText('POLYGON Z((206434.7179 447513.986 3, 206441.0581 447515.2454 3, 206434.6812 447526.476 3, 206434.088 447530.4191 3, 206424.8326 447544.0077 3, 206420.9923 447541.3965 3, 206420.3498 447541.2063 3, 206430.4453 447526.5241 3, 206430.9938 447526.8972 3, 206434.7179 447513.986 3))', 7415), ST_GeometryFromText('POLYGON((206420.3498 447513.986, 206420.3498 447544.0077, 206441.0581 447544.0077, 206441.0581 447513.986, 206420.3498 447513.986))', 7415), ST_GeometryFromText('POLYGON Z((6.136430874466896 52.013597716385306 1.123, 6.13652341067791 52.01360844678619 1.123, 6.136432215571403 52.01370997275932 1.123, 6.136424168944359 52.013745465524856 1.123, 6.136291399598122 52.01386845186669 1.123, 6.136235073208809 52.01384534036516 1.123, 6.136225685477257 52.013843689543165 1.123, 6.13637052476406 52.0137107981728 1.123, 6.136378571391106 52.013714099826544 1.123, 6.136430874466896 52.013597716385306 1.123))', 7931), false, null, null),
  (3,'28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL, ST_GeometryFromText('POLYGON Z((206417.6342 447594.7208 3, 206413.3982 447608.1774 3, 206404.4727 447603.172 3, 206407.438 447596.8657 3, 206408.8417 447594.676 3, 206409.8477 447595.3292 3, 206413.2217 447589.5782 3, 206417.6342 447594.7208 3))', 7415), ST_GeometryFromText('POLYGON((206404.4727 447589.5782, 206404.4727 447608.1774, 206417.6342 447608.1774, 206417.6342 447589.5782, 206404.4727 447589.5782))', 7415), ST_GeometryFromText('POLYGON Z((6.136194169521332 52.01432490157565 1.123, 6.13613449037075  52.01444623552498 1.123, 6.136003732681274 52.01440207660872 1.123, 6.136045977473259 52.01434512392339 1.123, 6.136066094040871 52.01432531427671 1.123, 6.136080846190453 52.014331092091226 1.123, 6.136129125952721 52.014279091733705 1.123, 6.136194169521332 52.01432490157565 1.123))', 7931), true, 1700, 'A king wanted a spicy beer');

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


CREATE FUNCTION dbeerpedia.breweries_history_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.breweries AS $$
   SELECT * FROM dbeerpedia.breweries
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.beers_history_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.beers AS $$
   SELECT * FROM dbeerpedia.beers WHERE daterange(valid_start, valid_end) @> $1 and tstzrange(available_start, available_end) @> $2
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.addresses_history_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.addresses AS $$
   SELECT * FROM dbeerpedia.addresses
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.ingredients_history_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.ingredients AS $$
   SELECT * FROM dbeerpedia.ingredients
$$ language SQL immutable;

CREATE FUNCTION dbeerpedia.beers_ingredients_history_ctx(date,timestamp with time zone) RETURNS SETOF dbeerpedia.beers_ingredients AS $$
   SELECT * FROM dbeerpedia.beers_ingredients
$$ language SQL immutable;