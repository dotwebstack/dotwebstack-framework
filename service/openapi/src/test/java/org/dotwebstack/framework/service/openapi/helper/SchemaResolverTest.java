package org.dotwebstack.framework.service.openapi.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.swagger.v3.oas.models.OpenAPI;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.junit.jupiter.api.Test;

public class SchemaResolverTest {

  private OpenAPI openApi = TestResources.openApi();


  @Test
  public void resolveSchema_returnsSchema_forComponent() {
    // Act / Assert
    SchemaResolver.resolveSchema(openApi, "#/components/schemas/Object1");
  }

  @Test
  public void resolveSchema_returnsSchema_forHeader() {
    // Act / Assert
    SchemaResolver.resolveSchema(openApi, "#/components/headers/HeaderRef");
  }

  @Test
  public void resolveSchema_throwsException_forInvalidRefStart() {
    // Act / Assert
    assertThrows(InvalidOpenApiConfigurationException.class,
        () -> SchemaResolver.resolveSchema(openApi, "#/headers/HeaderRef"));
  }

  @Test
  public void resolveSchema_throwsException_forInvalidRefComponent() {
    // Act / Assert
    assertThrows(InvalidOpenApiConfigurationException.class,
        () -> SchemaResolver.resolveSchema(openApi, "#/components/something/headerRef"));
  }
}
