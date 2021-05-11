package org.dotwebstack.framework.service.openapi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.junit.jupiter.api.Test;

class SchemaResolverTest {

  private OpenAPI openApi = TestResources.openApi();


  @Test
  void resolveSchema_returnsSchema_forComponent() {
    List<String> requiredList = Arrays.asList("o1_array1", "o1_prop1", "o1_prop2", "o1_prop5");

    var resolvedSchema = SchemaResolver.resolveSchema(openApi, "#/components/schemas/Object1");

    assertEquals(requiredList, resolvedSchema.getRequired());
  }

  @Test
  void resolveSchema_returnsSchema_forHeader() {
    Map<String, Object> extensions = Map.of("x-dws-expr", "`ref`");

    var resolvedSchema = SchemaResolver.resolveSchema(openApi, "#/components/headers/HeaderRef");

    assertEquals(extensions, resolvedSchema.getExtensions());
  }

  @Test
  void resolveSchema_throwsException_forInvalidRefStart() {
    assertThrows(InvalidOpenApiConfigurationException.class,
        () -> SchemaResolver.resolveSchema(openApi, "#/headers/HeaderRef"));
  }

  @Test
  void resolveSchema_throwsException_forInvalidRefComponent() {
    assertThrows(InvalidOpenApiConfigurationException.class,
        () -> SchemaResolver.resolveSchema(openApi, "#/components/something/headerRef"));
  }
}
