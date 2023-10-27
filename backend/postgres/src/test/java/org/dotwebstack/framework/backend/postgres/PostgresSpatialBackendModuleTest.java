package org.dotwebstack.framework.backend.postgres;

import static graphql.Assert.assertTrue;
import static org.dotwebstack.framework.backend.postgres.PostgresSpatialBackendModule.FOREIGNKEYS_SEGMENT_TABLE_STMT;
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
import org.dotwebstack.framework.backend.postgres.model.GeometrySegmentsTable;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class PostgresSpatialBackendModuleTest {

  @Test
  void contructor_throwsException_onError() {
    var schema = mock(Schema.class);
    var postgresClient = mockDatabaseCalls(Flux.error(new RuntimeException("Something went wrong.")));

    Exception exception = Assertions.assertThrows(InvalidConfigurationException.class, () -> {
      new PostgresSpatialBackendModule(schema, postgresClient);
    });

    String expectedMessage = "Something went wrong.";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.equals(expectedMessage));
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
    var postgresClient = mockDatabaseCalls(Flux.empty());

    assertDoesNotThrow(() -> new PostgresSpatialBackendModule(schema, postgresClient).init(null));
  }

  @Test
  void init_setSpatial_whenCalled() {
    var schema = new Schema();
    schema.setObjectTypes(createObjectTypes());

    var postgresClient = mockDatabaseCalls();
    var spatial = createSpatial();

    var spatialBackendModule = new PostgresSpatialBackendModule(schema, postgresClient);
    spatialBackendModule.init(spatial);

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

    assertThat(brewerySpatial.getSegmentsTable()
        .isPresent(), is(true));
    assertThat(addressSpatial.getSegmentsTable()
        .isPresent(), is(false));

    var expectedSegmentsTable = createdExpectedSegmentTable();
    assertThat(brewerySpatial.getSegmentsTable()
        .get(), is(expectedSegmentsTable));
  }

  private GeometrySegmentsTable createdExpectedSegmentTable() {
    var joinColumn = new JoinColumn();
    joinColumn.setName("brewery__record_id");
    joinColumn.setReferencedColumn("record_id");

    return new GeometrySegmentsTable("dbeerpedia", "brewery__brewery_geometry__segments", "brewery_geometry",
        List.of(joinColumn));
  }

  private Map<String, ObjectType<? extends ObjectField>> createObjectTypes() {
    var address = new PostgresObjectType();
    address.setFields(createAddressFields());

    var company = new PostgresObjectType();
    company.setFields(createCompanyFields(company));
    var companyRelatie = new PostgresObjectType();
    companyRelatie.setFields(createCompanyRelatieFields(company));

    var brewery = new PostgresObjectType();
    brewery.setTable("brewery");
    brewery.setFields(createBreweryFields(address, companyRelatie));

    return Map.of("Brewery", brewery, "Address", address);
  }

  private Map<String, PostgresObjectField> createBreweryFields(ObjectType<? extends ObjectField> addressType,
      ObjectType<? extends ObjectField> companyRelatieType) {
    var location = new PostgresObjectField();
    location.setName("breweryGeometry");
    location.setColumn("brewery_geometry");
    location.setType("Geometry");

    var address = new PostgresObjectField();
    address.setName("address");
    address.setType("Address");
    address.setTargetType(addressType);

    var isPartOf = new PostgresObjectField();
    isPartOf.setName("isPartOf");
    isPartOf.setType("CompanyRelatie");
    isPartOf.setTargetType(companyRelatieType);

    return Map.of("breweryGeometry", location, "address", address, "isPartOf", isPartOf);
  }

  private Map<String, PostgresObjectField> createAddressFields() {
    var location = new PostgresObjectField();
    location.setName("addressGeometry");
    location.setColumn("address_geometry");
    location.setType("Geometry");
    return Map.of("addressGeometry", location);
  }

  private Map<String, PostgresObjectField> createCompanyFields(ObjectType<? extends ObjectField> companyType) {
    var isPartOf = new PostgresObjectField();
    isPartOf.setName("isPartOf");
    isPartOf.setType("Company");
    isPartOf.setTargetType(companyType);
    return Map.of("isPartOf", isPartOf);
  }

  private Map<String, PostgresObjectField> createCompanyRelatieFields(ObjectType<? extends ObjectField> companyType) {
    var isPartOf = new PostgresObjectField();
    isPartOf.setName("isPartOf");
    isPartOf.setType("Company");
    isPartOf.setTargetType(companyType);
    isPartOf.setList(true);
    var joinColumn = new JoinColumn();
    joinColumn.setName("identificatie");
    joinColumn.setReferencedColumn("is_part_of__identificatie");
    isPartOf.setJoinColumns(List.of(joinColumn));
    return Map.of("isPartOf", isPartOf);
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

  private PostgresClient mockDatabaseCalls() {
    var postgresClient = mock(PostgresClient.class);

    Map<String, Object> segmnentTableRow = Map.of("f_table_schema", "dbeerpedia", "f_table_name",
        "brewery__brewery_geometry__segments", "f_geometry_column", "brewery_geometry");
    var segmentTablesQuery = "SELECT f_table_schema, f_table_name, f_geometry_column "
        + "FROM geometry_columns where f_table_name LIKE '%__segments'";
    when(postgresClient.fetch(segmentTablesQuery)).thenReturn(Flux.just(segmnentTableRow));

    var geometryColumnsQuery = "SELECT f_table_schema, f_table_name, f_geometry_column, srid FROM geometry_columns "
        + "UNION ALL "
        + "SELECT f_table_schema, f_table_name, f_geography_column as f_geometry_column, srid FROM geography_columns";
    when(postgresClient.fetch(geometryColumnsQuery)).thenReturn(createGeoSridRows());

    var foreignkeysQuery = String.format(FOREIGNKEYS_SEGMENT_TABLE_STMT, "brewery__brewery_geometry__segments");

    Map<String, Object> joinColumnRow =
        Map.of("join_column_name", "brewery__record_id", "referenced_column_name", "record_id");
    when(postgresClient.fetch(foreignkeysQuery)).thenReturn(Flux.just(joinColumnRow));
    return postgresClient;
  }

  private PostgresClient mockDatabaseCalls(Flux<Map<String, Object>> flux) {
    var postgresClient = mock(PostgresClient.class);
    when(postgresClient.fetch(anyString())).thenReturn(flux);
    return postgresClient;
  }

  private Flux<Map<String, Object>> createGeoSridRows() {
    var breweryGeometryRow7931 = createSridRow("brewery_geometry", 7931);
    var breweryGeometryRowBbox7931 = createSridRow("brewery_geometry_bbox", 7931);
    var breweryGeometryRow7415 = createSridRow("brewery_geometry_7415", 7415);
    var addressGeometryRow7931 = createSridRow("address_geometry", 7931);
    var addressGeometryRow7415 = createSridRow("address_geometry_7415", 7415);

    return Flux.just(breweryGeometryRow7931, breweryGeometryRowBbox7931, breweryGeometryRow7415, addressGeometryRow7931,
        addressGeometryRow7415);
  }

  private Map<String, Object> createSridRow(String geometryColumnName, Integer srid) {
    return Map.of("f_table_schema", "dbeerpedia", "f_table_name", "brewery", "f_geometry_column", geometryColumnName,
        "srid", srid);
  }
}
