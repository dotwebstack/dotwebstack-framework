package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.addEvaluatedDwsParameters;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateParameterExistence;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequestBodyNonexistent;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;

@ExtendWith(MockitoExtension.class)
class CoreRequestHelperTest {

  @Test
  void parameterValidation_throwsError_withNonexistentParam() {
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
  void parameterValidation_throwsError_withPartialNonexistentParams() {
    // arrange
    Set<String> schemaParams = Set.of("cat");
    Set<String> givenParams = Set.of("cat", "dog");

    // act & assert
    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> validateParameterExistence("path", schemaParams, givenParams), "");
    assertEquals(thrown.getMessage(), "The following request path parameters are not allowed on this endpoint: [dog]");
  }

  @Test
  void parameterValidation_doesNotThrowError_withOnlyExistingParam() {
    // arrange
    Set<String> schemaParams = Set.of("cat", "dog");
    Set<String> givenParams = Set.of("cat");

    // act & assert
    assertDoesNotThrow(() -> validateParameterExistence("path", schemaParams, givenParams));
  }

  @Test
  void parameterValidation_ThrowsError_withNonexistentRequestBody() {
    // arrange
    MockServerWebExchange mockServerWebExchange = MockServerWebExchange.from(MockServerHttpRequest.post("/")
        .contentType(MediaType.APPLICATION_JSON)
        .body("test"));
    ServerRequest request = ServerRequest.create(mockServerWebExchange, HandlerStrategies.withDefaults()
        .messageReaders());

    // act & assert
    assertThrows(InvalidConfigurationException.class, () -> validateRequestBodyNonexistent(request));
  }

  @Test
  void addEvaluatedDwsParameters_addsEvaluatedJexlParams() {
    // arrange
    Map<String, Object> inputParams = Collections.singletonMap("someParam", "someValue");
    Map<String, String> dwsParameters = Collections.singletonMap("dwsParam", "request.path()");

    ServerRequest request = Mockito.mock(ServerRequest.class);
    when(request.path()).thenReturn("/path");

    JexlEngine jexlEngine = new JexlBuilder().create();
    JexlHelper jexlHelper = new JexlHelper(jexlEngine);

    // act
    Map<String, Object> result = addEvaluatedDwsParameters(inputParams, dwsParameters, request, jexlHelper);

    // assert
    assertEquals(result.get("someParam"), "someValue");
    assertEquals(result.get("dwsParam"), "/path");
  }

  @Test
  void validateRequiredPath_DoesNotThrowError_withValidInput() {
    // Arrange
    GraphQlField field = GraphQlField.builder()
        .fields(List.of(GraphQlField.builder()
            .name("postalCode")
            .build()))
        .build();

    // Act & Assert
    assertDoesNotThrow(() -> CoreRequestHelper.validateRequiredField(field, "postalCode", "breweries"));
  }

  @Test
  void validateRequiredPath_DoesNotThrowError_withValidNestedInput() {
    // Arrange
    GraphQlField field = GraphQlField.builder()
        .fields(List.of(GraphQlField.builder()
            .name("beers")
            .fields(List.of(GraphQlField.builder()
                .name("id")
                .build()))
            .build()))
        .build();

    // Act & Assert
    assertDoesNotThrow(() -> CoreRequestHelper.validateRequiredField(field, "beers.id", "breweries"));
  }

  @Test
  void validateRequiredPath_ThrowsErrorWithInvalidInput() {
    // Arrange
    GraphQlField field = GraphQlField.builder()
        .fields(List.of(GraphQlField.builder()
            .name("beers")
            .fields(List.of(GraphQlField.builder()
                .name("id")
                .build()))
            .build()))
        .build();

    // Act & Assert
    assertThrows(InvalidOpenApiConfigurationException.class,
        () -> CoreRequestHelper.validateRequiredField(field, "beers.test", "breweries"));
  }
}
