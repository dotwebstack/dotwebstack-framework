INSERT INTO db.address(identifier, street, city) VALUES
  ('fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'Church Street', 'Dublin'),
  ('3fe6c706-54af-4420-89c4-926ff719236a', '5th Avenue', 'New York');

INSERT INTO db.brewery(identifier, name, status, postal_address, visit_address) VALUES
  ('d3654375-95fa-46b4-8529-08b0f777bd6b', 'Brewery X', 'active', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f', 'fcb73181-a1b0-4748-8ae0-b7b51dd6497f'),
  ('6e8f89da-9676-4cb9-801b-aeb6e2a59ac9', 'Brewery Y', 'active', '3fe6c706-54af-4420-89c4-926ff719236a', NULL),
  ('28649f76-ddcf-417a-8c1d-8e5012c31959', 'Brewery Z', 'inactive', NULL, NULL);

INSERT INTO db.beer(identifier, name, abv, brewery) VALUES
  ('b0e7cf18-e3ce-439b-a63e-034c8452f59c', 'Beer 1', 5.4, 'd3654375-95fa-46b4-8529-08b0f777bd6b'),
  ('1295f4c1-846b-440c-b302-80bbc1f9f3a9', 'Beer 2', 4.7, 'd3654375-95fa-46b4-8529-08b0f777bd6b'),
  ('973832e7-1dd9-4683-a039-22390b1c1995', 'Beer 3', 8.0, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9'),
  ('a5148422-be13-452a-b9fa-e72c155df3b2', 'Beer 4', 9.5, 'd3654375-95fa-46b4-8529-08b0f777bd6b'),
  ('766883b5-3482-41cf-a66d-a81e79a4f0ed', 'Beer 5', 6.2, '6e8f89da-9676-4cb9-801b-aeb6e2a59ac9');

INSERT INTO db.ingredient(identifier, name) VALUES
  ('cd795192-5fbb-11eb-ae93-0242ac130002', 'Water'),
  ('cd794c14-5fbb-11eb-ae93-0242ac130002', 'Hop'),
  ('cd795196-5fbb-11eb-ae93-0242ac130002', 'Barley'),
  ('cd795191-5fbb-11eb-ae93-0242ac130002', 'Yeast'),
  ('cd79538a-5fbb-11eb-ae93-0242ac130002', 'Orange'),
  ('cd79545c-5fbb-11eb-ae93-0242ac130002', 'Caramel');

INSERT INTO db.beer_ingredient(beer_identifier, ingredient_identifier) VALUES
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