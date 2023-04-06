package org.dotwebstack.framework.core.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.config.validators.FilterValidator;
import org.dotwebstack.framework.core.config.validators.SchemaValidator;
import org.dotwebstack.framework.core.config.validators.SortValidator;
import org.dotwebstack.framework.core.testhelpers.TestBackendLoaderFactory;
import org.dotwebstack.framework.core.testhelpers.TestBackendModule;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
class ModelConfigurationTest {

  @Mock
  private DatabaseClient databaseClient;

  private ModelConfiguration modelConfiguration;

  private List<SchemaValidator> validatorList;

  @BeforeEach
  void setUp() {
    BackendModule<?> backendModule = new TestBackendModule(new TestBackendLoaderFactory(databaseClient));
    modelConfiguration = new ModelConfiguration(backendModule);
    validatorList = List.of(new FilterValidator(), new SortValidator());
  }

  @Test
  void schema_returnsSchema() throws IOException {
    String path = "src/test/resources/config/dotwebstack/dotwebstack-objecttypes-with-interfaces.yaml";

    File file = new File(path);
    String localUrl = file.toURI()
        .toURL()
        .toExternalForm();

    var result = modelConfiguration.schema(localUrl, validatorList);

    assertThat(result, CoreMatchers.is(notNullValue()));
  }

  @ParameterizedTest
  @CsvSource({
      "src/test/resources/config/dotwebstack/dotwebstack-objecttypes-with-incorrect-interfaces-on-objecttypes.yaml,"
          + " ObjectType, Brewery",
      "src/test/resources/config/dotwebstack/dotwebstack-objecttypes-with-incorrect-interfaces-on-interfaces.yaml,"
          + " Interface, Organization"})
  void schema_throwsException_whenNonExistentInterfaceConfigured(String path, String type, String objectName)
      throws IOException {
    File file = new File(path);
    String localUrl = file.toURI()
        .toURL()
        .toExternalForm();

    var exception =
        assertThrows(InvalidConfigurationException.class, () -> modelConfiguration.schema(localUrl, validatorList));

    assertThat(exception.getMessage(),
        is(String.format("Implemented Interface 'NonExistentInterface' not found in provided schema for %s '%s'.", type,
            objectName)));
  }
}
