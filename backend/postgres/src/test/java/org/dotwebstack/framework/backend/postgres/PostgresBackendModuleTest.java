package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
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
    postgresBackendModule = new PostgresBackendModule(backendLoaderFactory);
    testHelper = new TestHelper(postgresBackendModule);
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
    var objectTypes = (Map<String, ObjectType<?>>) schema.getObjectTypes();

    postgresBackendModule.init(objectTypes);
  }
}
