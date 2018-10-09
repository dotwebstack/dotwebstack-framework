package org.dotwebstack.framework.frontend.openapi.testutils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import io.swagger.v3.oas.models.OpenAPI;
import org.dotwebstack.framework.frontend.openapi.testutils.OpenApiToString.ToOpenApi3String;
import org.dotwebstack.framework.frontend.openapi.testutils.ToOpenApi.ToOpenApi3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OpenApiConvertersTest {

  @ParameterizedTest
  @CsvSource({"testutils/PostWithValidData.yml"})
  void convertToOpenApiModel(@ToOpenApi3 OpenAPI model) {
    assertThat(model, is(notNullValue()));
    assertThat(model.getInfo().getTitle(), is("API"));
  }

  @ParameterizedTest
  @CsvSource({"testutils/PostWithValidData.yml"})
  void ConvertToString(@ToOpenApi3String String model) {
    assertThat(model, is(notNullValue()));
    assertThat(model.contains("API"), is(true));
  }
}
