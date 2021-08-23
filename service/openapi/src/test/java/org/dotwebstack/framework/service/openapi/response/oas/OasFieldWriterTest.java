package org.dotwebstack.framework.service.openapi.response.oas;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OasFieldWriterTest {

  private OpenAPI openApi;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
  }

  @Test
  void toString_succeeds() {
    OasFieldBuilder builder = new OasFieldBuilder(this.openApi);
    List<String> stringValues = openApi.getComponents()
        .getSchemas()
        .values()
        .stream()
        .map(builder::build)
        .map(OasFieldWriter::toString)
        .collect(Collectors.toList());

    stringValues.forEach(s -> assertFalse(s.isEmpty()));
  }
}
