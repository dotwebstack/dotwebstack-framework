package org.dotwebstack.framework.backend.postgres;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
    Map<String, ObjectType<?>> objectTypes = init("src/test/resources/config/dotwebstack/dotwebstack-objecttypes.yaml");

    assertThat(objectTypes, notNullValue());

    var brewery = (PostgresObjectType) objectTypes.get("Brewery");
    assertThat(brewery, notNullValue());
    assertThat(brewery.getFields()
        .get("beerAgg"), notNullValue());
  }

  @Test
  void init_shouldPropagateNestedColumnPrefix() throws MalformedURLException {
    Map<String, ObjectType<?>> objectTypes =
        init("src/test/resources/config/dotwebstack/dotwebstack-objecttypes-with-column-prefix.yaml");

    assertThat(objectTypes, notNullValue());

    var beer = (PostgresObjectType) objectTypes.get("Beer");
    assertThat(beer, notNullValue());

    assertColumnPrefixFields(beer, "location", "loc_");
    assertColumnPrefixFields(beer, "altLocation", "altloc_");
    assertColumnPrefixFields(beer, "anotherLocation", "");
  }

  private void assertColumnPrefixFields(PostgresObjectType beer, String fieldName, String columnPrefix) {
    PostgresObjectField location = beer.getField(fieldName);
    assertThat(location, notNullValue());
    assertThat(location.getTargetType(), notNullValue());
    assertThat(beer.getField(fieldName)
        .getTargetType()
        .getField("street"), notNullValue());
    assertThat(((PostgresObjectField) beer.getField(fieldName)
        .getTargetType()
        .getField("street")).getColumn(), is(columnPrefix.concat("street")));
    assertThat(beer.getField(fieldName)
        .getTargetType()
        .getField("housenumber"), notNullValue());
    assertThat(((PostgresObjectField) beer.getField(fieldName)
        .getTargetType()
        .getField("housenumber")).getColumn(), is(columnPrefix.concat("housenumber")));
    assertThat(beer.getField(fieldName)
        .getTargetType()
        .getField("city"), notNullValue());
    assertThat(((PostgresObjectField) beer.getField(fieldName)
        .getTargetType()
        .getField("city")).getColumn(), is("ownprefix_city"));
  }

  private Map<String, ObjectType<?>> init(String path) throws MalformedURLException {
    File file = new File(path);
    String localUrl = file.toURI()
        .toURL()
        .toExternalForm();

    Schema schema = testHelper.getSchema(localUrl);
    Map<String, ObjectType<?>> objectTypes = schema.getObjectTypes();

    postgresBackendModule.init(objectTypes);
    return objectTypes;
  }
}
