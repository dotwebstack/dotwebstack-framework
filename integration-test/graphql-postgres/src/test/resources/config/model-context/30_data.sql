INSERT INTO db.address_i(identifier_address) VALUES
('fcb73181-a1b0-4748-8ae0-b7b51dd6497f'),
('3fe6c706-54af-4420-89c4-926ff719236a');

INSERT INTO db.address_v(identifier_address, street, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', 'New York');

INSERT INTO db.brewery_i(identifier_brewery) VALUES
('d3654375-95fa-46b4-8529-08b0f777bd6b'),
('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9'),
('28649f76-ddcf-417a-8c1d-8e5012c31959'),
('28649f76-ddcf-417a-8c1d-8e5012c11666');

INSERT INTO db.brewery_v(identifier_brewery, name, status, postal_address, visit_address, geometry, his_age, his_history, multinational, valid_start, valid_end, available_start, available_end) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f','POLYGON((5.971385957936759 52.22549347648849,5.972053827981467 52.22549347648849,5.972053827981467 52.225279885758624,5.971385957936759 52.225279885758624,5.971385957936759 52.22549347648849))', 1988, 'hip and new',true,'2018-01-01',null,'2018-02-01T12:00:00Z',null),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL,'POLYGON((6.171943938628401 51.48699391333906,6.174325740233626 51.48699391333906,6.174325740233626 51.48564437918149,6.171943938628401 51.48564437918149,6.171943938628401 51.48699391333906))', 1900, 'A long time ago',false,'2018-01-01',null,'2018-02-01T12:00:00Z',null),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL,'POLYGON((6.81534412421009 52.207653734352334,6.818358927142829 52.207653734352334,6.818358927142829 52.206457132522104,6.81534412421009 52.206457132522104,6.81534412421009 52.207653734352334))', 1700, 'A king wanted a spicy beer',false,'2018-01-01',null,'2018-02-01T12:00:00Z',null),
  ('28649f76-ddcf-417a-8c1d-8e5012c11666', 'Brewery S', 'active', NULL, NULL,'POLYGON((5.606214102087255 51.517908855471276,5.608499344167943 51.517908855471276,5.608499344167943 51.51677388177432,5.606214102087255 51.51677388177432,5.606214102087255 51.517908855471276))', 1600, 'Old',false,'2018-01-01',null,'2018-02-01T12:00:00Z',null);

INSERT INTO db.beer_i(identifier_beer) VALUES
('b0e7cf18-e3ce-439b-a63e-034c8452f59c'),
('1295f4c1-846b-440c-b302-80bbc1f9f3a9'),
('973832e7-1dd9-4683-a039-22390b1c1995'),
('a5148422-be13-452a-b9fa-e72c155df3b2'),
('766883b5-3482-41cf-a66d-a81e79a4f0ed'),
('766883b5-3482-41cf-a66d-a81e79a4f666');

INSERT INTO db.beer_v(identifier_beer, name, abv, brewery, sold_per_year, taste, since, last_brewed, valid_start, valid_end, available_start, available_end) VALUES
('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1 validStart: 2018-01-01, availableStart: 2018-02-01T12:00:00Z', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 1000000, array['MEATY', 'FRUITY']::db.taste[],to_date('2010-01-01','YYYY-MM-DD'),to_timestamp('2020-08-12T20:17:46','YYYY-MM-DD HH24:MI:SS'),'2018-01-01',null,'2018-02-01T12:00:00Z','2019-04-01T12:00:00Z'),
('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1 validStart: 2018-01-01, availableStart: 2019-04-01T12:00:00Z', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 1000000, array['MEATY', 'FRUITY']::db.taste[],to_date('2010-01-01','YYYY-MM-DD'),to_timestamp('2020-08-12T20:17:46','YYYY-MM-DD HH24:MI:SS'),'2018-01-01','2019-01-01','2019-04-01T12:00:00Z',null),
('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z', 5.8, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 1000000, array['MEATY', 'FRUITY']::db.taste[],to_date('2010-01-01','YYYY-MM-DD'),to_timestamp('2020-08-12T20:17:46','YYYY-MM-DD HH24:MI:SS'),'2019-01-01',null,'2019-04-01T12:00:00Z',null),
('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 500000, array['MEATY', NULL, 'SMOKY', 'WATERY', 'FRUITY']::db.taste[],to_date('2013-01-01','YYYY-MM-DD'),to_timestamp('2018-02-12T20:12:40','YYYY-MM-DD HH24:MI:SS'),'2018-01-01',null,'2018-02-01T12:00:00Z',null),
('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 250000, array['MEATY', 'SMOKY', 'SMOKY']::db.taste[],to_date('2016-01-01','YYYY-MM-DD'),to_timestamp('2021-01-24T12:10:00','YYYY-MM-DD HH24:MI:SS'),'2018-01-01',null,'2018-02-01T12:00:00Z',null),
('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 200000, NULL,to_date('2018-03-01','YYYY-MM-DD'),to_timestamp('2020-03-12T11:11:00','YYYY-MM-DD HH24:MI:SS'),'2018-01-01',null,'2018-02-01T12:00:00Z',null),
('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 100000, array['MEATY', 'SPICY']::db.taste[],to_date('2019-06-01','YYYY-MM-DD'),to_timestamp('2020-03-12T20:20:20','YYYY-MM-DD HH24:MI:SS'),'2018-01-01',null,'2018-02-01T12:00:00Z',null),
('766883b5-3482-41cf-a66d-a81e79a4f666', 'Beer 6', 6.0, '28649f76-ddcf-417a-8c1d-8e5012c11666', 50000, array['MEATY', 'WATERY']::db.taste[],to_date('2020-09-01','YYYY-MM-DD'),to_timestamp('2020-04-15T00:00:05','YYYY-MM-DD HH24:MI:SS'),'2018-01-01',null,'2018-02-01T12:00:00Z',null);