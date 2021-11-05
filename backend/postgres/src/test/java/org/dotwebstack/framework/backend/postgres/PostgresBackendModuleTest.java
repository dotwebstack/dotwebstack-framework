package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
class PostgresBackendModuleTest {

  @Mock
  private DatabaseClient databaseClient;

  private PostgresBackendLoaderFactory backendLoaderFactory;

  private PostgresBackendModule postgresBackendModule;

  private TestHelper testHelper;

  @BeforeEach
  void doBeforeEach() {
    backendLoaderFactory = new PostgresBackendLoaderFactory(databaseClient);
    postgresBackendModule = new PostgresBackendModule(getSpatial(), backendLoaderFactory);
    testHelper = new TestHelper(postgresBackendModule);
  }

  private Optional<Spatial> getSpatial() {
    var spatial = new Spatial();
    var srs = new PostgresSpatialReferenceSystem();
    spatial.setReferenceSystems(Map.of(7415, srs));
    return Optional.of(spatial);
  }

  @Test
  void getObjectTypeClass_returnsPostgresObjectTypeClass() {
    assertThat(postgresBackendModule.getObjectTypeClass(), CoreMatchers.is(PostgresObjectType.class));
  }

  @Test
  void getBackendLoaderFactory_returnsBackendLoaderFactory() {
    assertThat(postgresBackendModule.getBackendLoaderFactory(), CoreMatchers.is(backendLoaderFactory));
  }

  @Test
  void init_shouldInitFields() throws MalformedURLException {
    String path = "src/test/resources/config/dotwebstack/dotwebstack-objecttypes.yaml";
    File file = new File(path);
    String localUrl = file.toURI()
        .toURL()
        .toExternalForm();

    Schema schema = testHelper.getSchema(localUrl);
    Map<String, ObjectType<?>> objectTypes = schema.getObjectTypes();

    postgresBackendModule.init(objectTypes);

    assertThat(objectTypes, notNullValue());

    var brewery = (PostgresObjectType) objectTypes.get("Brewery");
    assertThat(brewery, notNullValue());
    assertThat(brewery.getFields()
        .get("beerAgg"), notNullValue());
  }
}
