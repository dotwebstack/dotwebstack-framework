package org.dotwebstack.framework.service.openapi.response;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasFieldBuilder;
import org.dotwebstack.framework.service.openapi.response.oas.OasFieldWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OasResponseBuilderTest {

  private OpenAPI openApi;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
  }

  @Test
  void go() {
    OasFieldBuilder builder = new OasFieldBuilder(this.openApi);
    openApi.getComponents()
        .getSchemas()
        .entrySet()
        .stream()
        .limit(2)
        .forEach(e -> {
          Schema<?> s = e.getValue();
          System.out.println("OBJECT " + e.getKey());
          OasField response = builder.build(s);
          System.out.println(OasFieldWriter.toString(response));
        });


  }

}
