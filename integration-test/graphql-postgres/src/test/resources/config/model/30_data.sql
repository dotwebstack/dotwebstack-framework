INSERT INTO db.address(identifier_address, street, street_tsv, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street',to_tsvector('Ch Chu Chur Churc Church St Str Stre Stree Street'), 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue',to_tsvector('5t 5th Av Ave Aven Avenu Avenue'), 'New York'),
  ('6fe507d8-a09b-40e7-a33b-df285935c651', 'Caste hill Rd', to_tsvector('Ca Cas Cast Castl Caslte Hi Hil Hill Rd'), 'Sydney');

INSERT INTO db.brewery(identifier_brewery, name, status, postal_address, visit_address, geometry, geometry_bbox, geometry_etrs89, his_age, his_history, multinational) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', ST_GeometryFromText('POLYGON Z((206410.1605 447480.1649 3, 206412.3799 447474.7692 3, 206418.7599 447476.6259 3, 206417.4787 447480.3322 3, 206423.1208 447482.3191 3, 206423.0706 447482.7319 3, 206416.4167 447480.6427 3, 206415.9896 447481.8782 3, 206410.1605 447480.1649 3))', 7415), ST_GeometryFromText('POLYGON((206410.1605 447474.7692, 206410.1605 447482.7319, 206423.1208 447482.7319, 206423.1208 447474.7692, 206410.1605 447474.7692))', 7415), ST_GeometryFromText('POLYGON Z((6.136068105697632 52.01329602598457 1.123, 6.136099621653557 52.01324732611223 1.123, 6.136192828416824 52.013263421838616 1.123, 6.136174723505974 52.013296851405684 1.123, 6.136257201433181 52.01331418524545 1.123, 6.136256530880928 52.01331789963881 1.123, 6.136159300804138 52.01329974037945 1.123, 6.136153265833855 52.01331088356219 1.123, 6.136068105697632 52.01329602598457 1.123))', 7931), 1988, 'hip and new', true),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL, ST_GeometryFromText('POLYGON Z((206434.7179 447513.986 3, 206441.0581 447515.2454 3, 206434.6812 447526.476 3, 206434.088 447530.4191 3, 206424.8326 447544.0077 3, 206420.9923 447541.3965 3, 206420.3498 447541.2063 3, 206430.4453 447526.5241 3, 206430.9938 447526.8972 3, 206434.7179 447513.986 3))', 7415), ST_GeometryFromText('POLYGON((206420.3498 447513.986, 206420.3498 447544.0077, 206441.0581 447544.0077, 206441.0581 447513.986, 206420.3498 447513.986))', 7415), ST_GeometryFromText('POLYGON Z((6.136430874466896 52.013597716385306 1.123, 6.13652341067791 52.01360844678619 1.123, 6.136432215571403 52.01370997275932 1.123, 6.136424168944359 52.013745465524856 1.123, 6.136291399598122 52.01386845186669 1.123, 6.136235073208809 52.01384534036516 1.123, 6.136225685477257 52.013843689543165 1.123, 6.13637052476406 52.0137107981728 1.123, 6.136378571391106 52.013714099826544 1.123, 6.136430874466896 52.013597716385306 1.123))', 7931), 1900, 'A long time ago', false),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', '6fe507d8-a09b-40e7-a33b-df285935c651', NULL, ST_GeometryFromText('POLYGON Z((206417.6342 447594.7208 3, 206413.3982 447608.1774 3, 206404.4727 447603.172 3, 206407.438 447596.8657 3, 206408.8417 447594.676 3, 206409.8477 447595.3292 3, 206413.2217 447589.5782 3, 206417.6342 447594.7208 3))', 7415), ST_GeometryFromText('POLYGON((206404.4727 447589.5782, 206404.4727 447608.1774, 206417.6342 447608.1774, 206417.6342 447589.5782, 206404.4727 447589.5782))', 7415), ST_GeometryFromText('POLYGON Z((6.136194169521332 52.01432490157565 1.123, 6.13613449037075  52.01444623552498 1.123, 6.136003732681274 52.01440207660872 1.123, 6.136045977473259 52.01434512392339 1.123, 6.136066094040871 52.01432531427671 1.123, 6.136080846190453 52.014331092091226 1.123, 6.136129125952721 52.014279091733705 1.123, 6.136194169521332 52.01432490157565 1.123))', 7931), null, null, false),
  ('28649f76-ddcf-417a-8c1d-8e5012c11666', 'Brewery S', 'active', '6fe507d8-a09b-40e7-a33b-df285935c651', NULL, ST_GeometryFromText('POLYGON Z((206391.0712 447768.8921 3, 206386.9222 447778.4003 3, 206374.6411 447772.9473 3, 206375.3946 447771.302 3, 206374.1104 447770.8296 3, 206375.4315 447767.7208 3, 206376.8067 447768.286 3, 206379.1593 447763.351 3, 206391.0712 447768.8921 3))', 7415), ST_GeometryFromText('POLYGON((206374.1104 447763.351,206374.1104 447778.4003,206391.0712 447778.4003,206391.0712 447763.351,206374.1104 447763.351))', 7415), ST_GeometryFromText('POLYGON Z((6.135833412408829 52.01589272546039 1.123, 6.135774403810501 52.015978564191975 1.123, 6.135594695806503 52.015930692611974 1.123, 6.135605424642563 52.01591583590432 1.123, 6.135586649179459 52.0159117090402 1.123, 6.135605424642563 52.015883646354155 1.123, 6.135625541210175 52.01588859859415 1.123, 6.135659068822861 52.01584402841444 1.123, 6.135833412408829 52.01589272546039 1.123))', 7931), 1600, 'Old', null);

INSERT INTO db.beer(identifier_beer, name, abv, brewery, sold_per_year, taste, since, last_brewed, predecessor) VALUES
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 1000000, array['MEATY', 'FRUITY']::db.taste[],to_date('2010-01-01','YYYY-MM-DD'),to_timestamp('2020-08-12T20:17:46','YYYY-MM-DD HH24:MI:SS'),null),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 500000, array['MEATY', NULL, 'SMOKY', 'WATERY', 'FRUITY']::db.taste[],to_date('2013-01-01','YYYY-MM-DD'),to_timestamp('2018-02-12T20:12:40','YYYY-MM-DD HH24:MI:SS'),'b0e7cf18-e3ce-439b-a63e-034c8452f59c'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 250000, array['MEATY', 'SMOKY', 'SMOKY']::db.taste[],to_date('2016-01-01','YYYY-MM-DD'),to_timestamp('2021-01-24T12:10:00','YYYY-MM-DD HH24:MI:SS'),null),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 'd3654375-95fa-46b4-8529-08b0f777bd6b', 200000, NULL,to_date('2018-03-01','YYYY-MM-DD'),to_timestamp('2020-03-12T11:11:00','YYYY-MM-DD HH24:MI:SS'),null),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 100000, array['MEATY', 'SPICY']::db.taste[],to_date('2019-06-01','YYYY-MM-DD'),to_timestamp('2020-03-12T20:20:20','YYYY-MM-DD HH24:MI:SS'),null),
  ('766883b5-3482-41cf-a66d-a81e79a4f666', 'Beer 6', 6.0, '28649f76-ddcf-417a-8c1d-8e5012c11666', 50000, array['MEATY', 'WATERY']::db.taste[],to_date('2020-09-01','YYYY-MM-DD'),to_timestamp('2020-04-15T00:00:05','YYYY-MM-DD HH24:MI:SS'),null);

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
