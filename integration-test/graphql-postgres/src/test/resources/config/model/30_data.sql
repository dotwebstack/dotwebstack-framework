INSERT INTO db.address(identifier_address, street, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', 'New York');

INSERT INTO db.brewery(identifier_brewery, name, status, postal_address, visit_address, geometry, his_age, his_history, multinational) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f',ST_Transform(ST_GeometryFromText('POLYGON((5.971385957936759 52.22549347648849,5.972053827981467 52.22549347648849,5.972053827981467 52.225279885758624,5.971385957936759 52.225279885758624,5.971385957936759 52.22549347648849))', 4326),7415), 1988, 'hip and new',true),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL,ST_Transform(ST_GeometryFromText('POLYGON((6.171943938628401 51.48699391333906,6.174325740233626 51.48699391333906,6.174325740233626 51.48564437918149,6.171943938628401 51.48564437918149,6.171943938628401 51.48699391333906))', 4326),7415), 1900, 'A long time ago',false),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL,ST_Transform(ST_GeometryFromText('POLYGON((6.81534412421009 52.207653734352334,6.818358927142829 52.207653734352334,6.818358927142829 52.206457132522104,6.81534412421009 52.206457132522104,6.81534412421009 52.207653734352334))', 4326),7415), 1700, 'A king wanted a spicy beer',false),
  ('28649f76-ddcf-417a-8c1d-8e5012c11666', 'Brewery S', 'active', NULL, NULL,ST_Transform(ST_GeometryFromText('POLYGON((5.606214102087255 51.517908855471276,5.608499344167943 51.517908855471276,5.608499344167943 51.51677388177432,5.606214102087255 51.51677388177432,5.606214102087255 51.517908855471276))', 4326),7415), 1600, 'Old',false);

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
