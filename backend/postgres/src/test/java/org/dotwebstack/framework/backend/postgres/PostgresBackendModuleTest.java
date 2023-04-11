package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresBackendModuleTest {

  @Mock
  private PostgresClient postgresClient;

  private PostgresBackendLoaderFactory backendLoaderFactory;

  private PostgresBackendModule postgresBackendModule;

  private TestHelper testHelper;

  @BeforeEach
  void doBeforeEach() {
    backendLoaderFactory = new PostgresBackendLoaderFactory(postgresClient);
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
    Map<String, ObjectType<?>> objectTypes = init();

    assertThat(objectTypes, notNullValue());

    var brewery = (PostgresObjectType) objectTypes.get("Brewery");
    assertThat(brewery, notNullValue());
    assertThat(brewery.getFields()
        .get("beerAgg"), notNullValue());

    var addresses = brewery.getFields()
        .get("addresses");
    assertThat(addresses, notNullValue());
    assertThat(addresses.getTargetType(), notNullValue());

    var addressIdentifier = (PostgresObjectField) addresses.getTargetType()
        .getField("identifier");
    assertThat(addressIdentifier, notNullValue());
    assertThat(addressIdentifier.getColumn(), equalTo("addresses__identifier"));

    var addressStreet = (PostgresObjectField) addresses.getTargetType()
        .getField("street");
    assertThat(addressStreet, notNullValue());
    assertThat(addressStreet.getColumn(), equalTo("custom_street_column"));
  }

  private Map<String, ObjectType<?>> init() throws MalformedURLException {
    File file = new File("src/test/resources/config/dotwebstack/dotwebstack-objecttypes.yaml");
    String localUrl = file.toURI()
        .toURL()
        .toExternalForm();

    Schema schema = testHelper.getSchema(localUrl);
    Map<String, ObjectType<?>> objectTypes = schema.getObjectTypes();

    postgresBackendModule.init(objectTypes);
    return objectTypes;
  }
}
