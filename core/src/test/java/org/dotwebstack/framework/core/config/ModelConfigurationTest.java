package org.dotwebstack.framework.core.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.dotwebstack.framework.core.TestBackendLoaderFactory;
import org.dotwebstack.framework.core.TestBackendModule;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.config.validators.FilterValidator;
import org.dotwebstack.framework.core.config.validators.SchemaValidator;
import org.dotwebstack.framework.core.config.validators.SettingsValidator;
import org.dotwebstack.framework.core.config.validators.SortValidator;
import org.dotwebstack.framework.core.model.Schema;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;

@ExtendWith(MockitoExtension.class)
public class ModelConfigurationTest {

  @Mock
  private DatabaseClient databaseClient;

  private BackendModule<?> backendModule;

  private ModelConfiguration modelConfiguration;

  private List<SchemaValidator> validatorList;

  @BeforeEach
  void setUp() {
    backendModule = new TestBackendModule(new TestBackendLoaderFactory(databaseClient));
    modelConfiguration = new ModelConfiguration(backendModule);
    validatorList = List.of(new FilterValidator(), new SettingsValidator(), new SortValidator());
  }

  @Test
  void schema_returnsSchema() throws IOException {
    String path = "src/test/resources/config/dotwebstack/dotwebstack-objecttypes.yaml";

    File file = new File(path);
    String localUrl = file.toURI()
        .toURL()
        .toExternalForm();

    var result = modelConfiguration.schema(localUrl, validatorList);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Schema);
  }
}
