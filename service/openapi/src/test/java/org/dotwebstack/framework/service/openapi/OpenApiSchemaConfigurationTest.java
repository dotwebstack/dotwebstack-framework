package org.dotwebstack.framework.service.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.OpenAPI;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class OpenApiSchemaConfigurationTest {

  @Test
  void test_openApiStream() throws FileNotFoundException {
    // Arrange
    OpenApiSchemaConfiguration openApiSchemaConfiguration = new OpenApiSchemaConfiguration();

    // Act
    InputStream stream = openApiSchemaConfiguration.openApiStream();

    // Assert
    assertNotNull(stream);
  }

  @Test
  void test_openApi() {
    // Arrange
    OpenApiSchemaConfiguration openApiSchemaConfiguration = new OpenApiSchemaConfiguration();

    // Act
    OpenAPI openApi = openApiSchemaConfiguration.openApi();

    // Assert
    assertNotNull(openApi);
  }
}
