package org.dotwebstack.framework.service.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.OpenAPI;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class OpenApiSchemaConfigurationTest {

  @Test
  void test_openApiStream() throws FileNotFoundException {
    OpenApiSchemaConfiguration openApiSchemaConfiguration = new OpenApiSchemaConfiguration();
    InputStream stream = openApiSchemaConfiguration.openApiStream();

    assertNotNull(stream);
  }

  @Test
  void test_openApi() {
    OpenApiSchemaConfiguration openApiSchemaConfiguration = new OpenApiSchemaConfiguration();
    OpenAPI openApi = openApiSchemaConfiguration.openApi();

    assertNotNull(openApi);
  }
}
