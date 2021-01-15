CREATE SCHEMA dbeerpedia;

CREATE TABLE dbeerpedia.addresses (
  identifier character varying NOT NULL PRIMARY KEY,
  street character varying NOT NULL,
  city character varying NOT NULL
);

INSERT INTO dbeerpedia.addresses(identifier, street, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', 'New York');

CREATE TABLE dbeerpedia.breweries (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  postal_address character varying REFERENCES dbeerpedia.addresses (identifier),
  visit_address character varying REFERENCES dbeerpedia.addresses (identifier)
);

INSERT INTO dbeerpedia.breweries(identifier, name, postal_address, visit_address) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f'),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', '3fe6c706-54af-4420-89c4-926ff719236a', NULL),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', NULL, NULL);

CREATE TABLE dbeerpedia.beers (
  identifier character varying NOT NULL PRIMARY KEY,
  name character varying NOT NULL,
  abv NUMERIC(3,1) NOT NULL,
  brewery character varying NOT NULL REFERENCES dbeerpedia.breweries (identifier)
);

CREATE INDEX brewery_idx ON dbeerpedia.beers (brewery);

INSERT INTO dbeerpedia.beers(identifier, name, abv, brewery) VALUES
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 'd3654375-95fa-46b4-8529-08b0f777bd6b'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 'd3654375-95fa-46b4-8529-08b0f777bd6b'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9');