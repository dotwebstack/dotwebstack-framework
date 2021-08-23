package org.dotwebstack.framework.service.openapi.response;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.swagger.v3.oas.models.OpenAPI;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.response.oas.OasFieldBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class OasResponseBuilderTest {

  private OpenAPI openApi;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
  }

  @Test
  void build_succeeds() {
    OasFieldBuilder builder = new OasFieldBuilder(this.openApi);

    assertDoesNotThrow(() -> {
      openApi.getComponents()
          .getSchemas()
          .forEach((key, value) -> builder.build(value));
    });
  }

}
