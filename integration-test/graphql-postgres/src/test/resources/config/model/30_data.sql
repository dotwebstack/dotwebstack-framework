INSERT INTO db.address(identifier_address, street, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', 'New York');

INSERT INTO db.brewery(identifier_brewery, name, status, postal_address, visit_address, geometry, his_age, his_history, multinational) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f',ST_GeometryFromText('POLYGON ((194914.7190916618 470984.86365462304, 194960.3511599757 470985.2315617077, 194960.54286521868 470961.4676284248, 194914.91057804757 470961.0997201087, 194914.7190916618 470984.86365462304))', 28992), 1988, 'hip and new',true),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL,ST_GeometryFromText('POLYGON ((209504.71741236537 388955.1797642, 209670.14222753374 388956.96401456, 209671.7639727657 388806.83245634363, 209506.3342512481 388805.048169459, 209504.71741236537 388955.1797642))', 28992), 1900, 'A long time ago',false),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL,ST_GeometryFromText('POLYGON ((252614.26315892197 469800.43795186176, 252820.3081096584 469804.50043428235, 252822.9354751886 469671.3829863825, 252616.88499025925 469667.32042779063, 252614.26315892197 469800.43795186176))', 28992), 1700, 'A king wanted a spicy beer',false),
  ('28649f76-ddcf-417a-8c1d-8e5012c11666', 'Brewery S', 'active', NULL, NULL,ST_GeometryFromText('POLYGON ((170201.50564017857 392123.8459786053, 170360.12206800058 392124.325549457, 170360.50581099375 391998.0582240514, 170201.8854233637 391997.5786449262, 170201.50564017857 392123.8459786053))', 28992), 1600, 'Old',false);

INSERT INTO db.beer(identifier_beer, name, abv, brewery, sold_per_year, taste, since, last_brewed) VALUES
('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 1000000, array['MEATY', 'FRUITY']::db.taste[],to_date('2010-01-01','YYYY-MM-DD'),to_timestamp('2020-08-12T20:17:46','YYYY-MM-DD HH24:MI:SS')),
('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 500000, array['MEATY', NULL, 'SMOKY', 'WATERY', 'FRUITY']::db.taste[],to_date('2013-01-01','YYYY-MM-DD'),to_timestamp('2018-02-12T20:12:40','YYYY-MM-DD HH24:MI:SS')),
('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 250000, array['MEATY', 'SMOKY', 'SMOKY']::db.taste[],to_date('2016-01-01','YYYY-MM-DD'),to_timestamp('2021-01-24T12:10:00','YYYY-MM-DD HH24:MI:SS')),
('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 200000, NULL,to_date('2018-03-01','YYYY-MM-DD'),to_timestamp('2020-03-12T11:11:00','YYYY-MM-DD HH24:MI:SS')),
('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 100000, array['MEATY', 'SPICY']::db.taste[],to_date('2019-06-01','YYYY-MM-DD'),to_timestamp('2020-03-12T20:20:20','YYYY-MM-DD HH24:MI:SS')),
('766883b5-3482-41cf-a66d-a81e79a4f666', 'Beer 6', 6.0, '28649f76-ddcf-417a-8c1d-8e5012c11666', 50000, array['MEATY', 'WATERY']::db.taste[],to_date('2020-09-01','YYYY-MM-DD'),to_timestamp('2020-04-15T00:00:05','YYYY-MM-DD HH24:MI:SS'));

INSERT INTO db.ingredient(identifier_ingredient, name, code, weight) VALUES
  ('cd795192-5fbb-11eb-ae93-0242ac130002', 'Water', 'WTR',1.2),
  ('cd794c14-5fbb-11eb-ae93-0242ac130002', 'Hop', 'HOP',1.2),
  ('cd795196-5fbb-11eb-ae93-0242ac130002', 'Barley','BRL', 3.4),
  ('cd795191-5fbb-11eb-ae93-0242ac130002', 'Yeast', 'YST', 4.8),
  ('cd79538a-5fbb-11eb-ae93-0242ac130002', 'Orange', 'RNG', 5.0),
  ('cd79545c-5fbb-11eb-ae93-0242ac130002', 'Caramel', 'CRM', 6.6);

INSERT INTO db.beer_ingredient(beer_identifier, ingredient_code) VALUES
  -- beer1
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'WTR'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'HOP'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'BRL'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'YST'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'RNG'),
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'CRM'),
  -- beer2
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'WTR'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'HOP'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'BRL'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'YST'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'RNG'),
  -- beer3
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'WTR'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'HOP'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'BRL'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'YST'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'CRM'),
  -- beer4
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'WTR'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'HOP'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'BRL'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'YST'),
  -- beer5
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'WTR'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'HOP'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'BRL'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'YST'),
  -- beer6
  ('766883b5-3482-41cf-a66d-a81e79a4f666', 'WTR');
