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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CoreRequestHelperTest {

  @Test
  void parameterValidation_throwsError_withNonexistentParam() {
    Set<String> schemaParams = Set.of("extends");
    Set<String> givenParams = new LinkedHashSet<>();
    givenParams.add("cat");
    givenParams.add("dog");

    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> validateParameterExistence("path", schemaParams, givenParams));
    assertEquals("The following request path parameters are not allowed on this endpoint: [cat, dog]",
        thrown.getMessage());
  }

  @Test
  void parameterValidation_throwsError_withPartialNonexistentParams() {
    Set<String> schemaParams = Set.of("cat");
    Set<String> givenParams = Set.of("cat", "dog");

    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> validateParameterExistence("path", schemaParams, givenParams), "");
    assertEquals("The following request path parameters are not allowed on this endpoint: [dog]", thrown.getMessage());
  }

  @Test
  void parameterValidation_doesNotThrowError_withOnlyExistingParam() {
    Set<String> schemaParams = Set.of("cat", "dog");
    Set<String> givenParams = Set.of("cat");

    assertDoesNotThrow(() -> validateParameterExistence("path", schemaParams, givenParams));
  }

  @Test
  void parameterValidation_ThrowsError_withNonexistentRequestBody() {
    ServerRequest request = MockServerRequest.builder()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .header(HttpHeaders.CONTENT_LENGTH, "2")
        .body(Mono.just("{}"));

    assertThrows(InvalidConfigurationException.class, () -> validateRequestBodyNonexistent(request));
  }

  @Test
  void addEvaluatedDwsParameters_addsEvaluatedJexlParams() {
    Map<String, Object> inputParams = Collections.singletonMap("someParam", "someValue");
    Map<String, String> dwsParameters = Collections.singletonMap("dwsParam", "request.path()");

    ServerRequest request = Mockito.mock(ServerRequest.class);
    when(request.path()).thenReturn("/path");

    JexlEngine jexlEngine = new JexlBuilder().create();
    JexlHelper jexlHelper = new JexlHelper(jexlEngine);

    Map<String, Object> result = addEvaluatedDwsParameters(inputParams, dwsParameters, request, jexlHelper);

    assertEquals("someValue", result.get("someParam"));
    assertEquals("/path", result.get("dwsParam"));
  }

  @Test
  void validateRequiredPath_DoesNotThrowError_withValidInput() {
    GraphQlField field = GraphQlField.builder()
        .fields(List.of(GraphQlField.builder()
            .name("postalCode")
            .build()))
        .build();

    assertDoesNotThrow(() -> CoreRequestHelper.validateRequiredField(field, "postalCode", "breweries"));
  }

  @Test
  void validateRequiredPath_DoesNotThrowError_withValidNestedInput() {
    GraphQlField field = GraphQlField.builder()
        .fields(List.of(GraphQlField.builder()
            .name("beers")
            .fields(List.of(GraphQlField.builder()
                .name("id")
                .build()))
            .build()))
        .build();

    assertDoesNotThrow(() -> CoreRequestHelper.validateRequiredField(field, "beers.id", "breweries"));
  }

  @Test
  void validateRequiredPath_ThrowsErrorWithInvalidInput() {
    GraphQlField field = GraphQlField.builder()
        .fields(List.of(GraphQlField.builder()
            .name("beers")
            .fields(List.of(GraphQlField.builder()
                .name("id")
                .build()))
            .build()))
        .build();

    assertThrows(InvalidOpenApiConfigurationException.class,
        () -> CoreRequestHelper.validateRequiredField(field, "beers.test", "breweries"));
  }
}
