package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class PostgresSpatialBackendModuleTest {

  @Test
  void contructor_throwsNoException_onError() {
    var schema = mock(Schema.class);
    var databaseClient = mockDatabaseCalls(Flux.error(new RuntimeException("Something went wrong.")));

    assertDoesNotThrow(() -> new PostgresSpatialBackendModule(schema, databaseClient));
  }

  @Test
  void getSpatialReferenceSystemClass_returnsPostgresSpatialReferenceSystem_forCall() {
    var schema = mock(Schema.class);
    var databaseClient = mockDatabaseCalls(Flux.empty());

    Class<?> result = new PostgresSpatialBackendModule(schema, databaseClient).getSpatialReferenceSystemClass();

    assertThat(result, is(PostgresSpatialReferenceSystem.class));
  }

  @Test
  void init_setSpatial_doesNothingWhenNull() {
    var schema = mock(Schema.class);
    var databaseClient = mockDatabaseCalls(Flux.empty());

    assertDoesNotThrow(() -> new PostgresSpatialBackendModule(schema, databaseClient).init(null));
  }

  @Test
  void init_setSpatial_whenCalled() {
    var schema = new Schema();
    schema.setObjectTypes(createObjectTypes());

    var databaseClient = mockDatabaseCalls(Flux.just(createRows()));

    var spatial = createSpatial();

    new PostgresSpatialBackendModule(schema, databaseClient).init(spatial);

    var brewerySpatial = ((PostgresObjectField) schema.getObjectTypes()
        .get("Brewery")
        .getField("breweryGeometry")).getSpatial();
    var addressSpatial = ((PostgresObjectField) schema.getObjectTypes()
        .get("Address")
        .getField("addressGeometry")).getSpatial();

    assertThat(brewerySpatial, is(notNullValue()));
    assertThat(addressSpatial, is(notNullValue()));

    assertThat(brewerySpatial.getSrid(), is(7931));
    assertThat(addressSpatial.getSrid(), is(7931));

    assertThat(brewerySpatial.getSpatialReferenceSystems(),
        allOf(hasEntry(is(7415), is("brewery_geometry_7415")), hasEntry(is(7931), is("brewery_geometry"))));
    assertThat(addressSpatial.getSpatialReferenceSystems(),
        allOf(hasEntry(is(7415), is("address_geometry_7415")), hasEntry(is(7931), is("address_geometry"))));

    assertThat(brewerySpatial.getBboxes(), allOf(hasEntry(is(7931), is("brewery_geometry_bbox"))));

    assertThat(brewerySpatial.getEquivalents(), allOf(hasEntry(is(7415), is(28892)), hasEntry(is(7931), is(9067))));
    assertThat(addressSpatial.getEquivalents(), allOf(hasEntry(is(7415), is(28892)), hasEntry(is(7931), is(9067))));
  }

  private Map<String, ObjectType<? extends ObjectField>> createObjectTypes() {
    var address = new PostgresObjectType();
    address.setFields(createAddressFields());

    var brewery = new PostgresObjectType();
    brewery.setTable("brewery");
    brewery.setFields(createBreweryFields(address));

    return Map.of("Brewery", brewery, "Address", address);
  }

  private Map<String, PostgresObjectField> createBreweryFields(ObjectType<? extends ObjectField> addressType) {
    var location = new PostgresObjectField();
    location.setName("breweryGeometry");
    location.setColumn("brewery_geometry");
    location.setType("Geometry");

    var address = new PostgresObjectField();
    address.setName("address");
    address.setType("Address");
    address.setTargetType(addressType);

    return Map.of("breweryGeometry", location, "address", address);
  }

  private Map<String, PostgresObjectField> createAddressFields() {
    var location = new PostgresObjectField();
    location.setName("addressGeometry");
    location.setColumn("address_geometry");
    location.setType("Geometry");
    return Map.of("addressGeometry", location);
  }

  private Spatial createSpatial() {
    Spatial spatial = new Spatial();
    spatial.setReferenceSystems(createSpatialReferenceSystems());
    return spatial;
  }

  private Map<Integer, SpatialReferenceSystem> createSpatialReferenceSystems() {
    var srs7931 = new PostgresSpatialReferenceSystem();
    srs7931.setDimensions(3);
    srs7931.setEquivalent(9067);
    srs7931.setBboxColumnSuffix("_bbox");

    var srs9067 = new PostgresSpatialReferenceSystem();
    srs9067.setDimensions(2);
    srs9067.setBboxColumnSuffix("_bbox");

    var srs7415 = new PostgresSpatialReferenceSystem();
    srs7415.setDimensions(3);
    srs7415.setEquivalent(28892);
    srs7415.setColumnSuffix("_7415");

    var srs28892 = new PostgresSpatialReferenceSystem();
    srs28892.setDimensions(2);
    srs28892.setColumnSuffix("_7415");

    return Map.of(7931, srs7931, 9067, srs9067, 7415, srs7415, 28892, srs28892);
  }

  @SuppressWarnings({"unchecked"})
  private DatabaseClient mockDatabaseCalls(Flux<Object> flux) {
    var databaseClient = mock(DatabaseClient.class);
    var executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
    var fetchSpec = mock(FetchSpec.class);

    when(databaseClient.sql(anyString())).thenReturn(executeSpec);
    when(executeSpec.fetch()).thenReturn(fetchSpec);
    when(fetchSpec.all()).thenReturn(flux);

    return databaseClient;
  }

  private Object[] createRows() {
    var breweryGeometryRow7931 = createRow("brewery_geometry", 7931);
    var breweryGeometryRowBbox7931 = createRow("brewery_geometry_bbox", 7931);
    var breweryGeometryRow7415 = createRow("brewery_geometry_7415", 7415);
    var addressGeometryRow7931 = createRow("address_geometry", 7931);
    var addressGeometryRow7415 = createRow("address_geometry_7415", 7415);

    return List
        .of(breweryGeometryRow7931, breweryGeometryRowBbox7931, breweryGeometryRow7415, addressGeometryRow7931,
            addressGeometryRow7415)
        .toArray();
  }

  private Map<String, Object> createRow(String geometryColumnName, Integer srid) {
    return Map.of("f_table_schema", "dbeerpedia", "f_table_name", "brewery", "f_geometry_column", geometryColumnName,
        "srid", srid);
  }
}
