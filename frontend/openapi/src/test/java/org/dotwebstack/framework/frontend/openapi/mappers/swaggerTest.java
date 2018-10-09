package org.dotwebstack.framework.frontend.openapi.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

import io.swagger.v3.oas.models.OpenAPI;
import org.dotwebstack.framework.frontend.openapi.testutils.OpenApiConverter.ToOpenApi3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class swaggerTest {

  @ParameterizedTest
  @CsvSource({"OAS3test.yml"})
  void doTestWithConvertedApiSpec(@ToOpenApi3 OpenAPI model) {
    assertThat(model, is(notNullValue()));
    System.out.println(model.getInfo().getTitle());
    assertThat(model.getInfo().getTitle(), is("API"));
  }
}
