package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateParameterExistence;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequestBodyNonexistent;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

class CoreRequestHelperTest {

  @Test
  public void parameterValidation_throwsError_withNonexistentParam() {
    // arrange
    Set<String> schemaParams = Set.of("extends");
    Set<String> givenParams = new LinkedHashSet<>();
    givenParams.add("cat");
    givenParams.add("dog");

    // act & assert
    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> validateParameterExistence("path", schemaParams, givenParams));
    assertEquals(thrown.getMessage(),
        "The following request path parameters are not allowed on this endpoint: [cat, dog]");
  }

  @Test
  public void parameterValidation_throwsError_withPartialNonexistentParams() {
    // arrange
    Set<String> schemaParams = Set.of("cat");
    Set<String> givenParams = Set.of("cat", "dog");

    // act & assert
    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> validateParameterExistence("path", schemaParams, givenParams), "");
    assertEquals(thrown.getMessage(), "The following request path parameters are not allowed on this endpoint: [dog]");
  }

  @Test
  public void parameterValidation_doesNotThrowError_withOnlyExistingParam() {
    // arrange
    Set<String> schemaParams = Set.of("cat", "dog");
    Set<String> givenParams = Set.of("cat");

    // act & assert
    assertDoesNotThrow(() -> validateParameterExistence("path", schemaParams, givenParams));
  }

  @Test
  public void parameterValidation_ThrowsError_withNonexistentRequestBody() {
    // arrange
    ServerRequest request = MockServerRequest.builder()
        .body(Mono.just("test"));

    // act & assert
    assertThrows(InvalidConfigurationException.class, () -> validateRequestBodyNonexistent(request));
  }
}
