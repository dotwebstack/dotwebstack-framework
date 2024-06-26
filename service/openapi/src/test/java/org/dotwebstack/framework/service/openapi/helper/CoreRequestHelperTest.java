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
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CoreRequestHelperTest {

  @Mock
  private EnvironmentProperties environmentProperties;

  @Test
  void parameterValidation_throwsError_withNonexistentParam() {
    Set<String> schemaParams = Set.of("extends");
    Set<String> givenParams = new LinkedHashSet<>();
    givenParams.add("cat");
    givenParams.add("dog");

    ParameterValidationException thrown = assertThrows(ParameterValidationException.class,
        () -> validateParameterExistence("path", schemaParams, givenParams));
    assertEquals("The following request path parameters are not allowed on this endpoint: [cat, dog]",
        thrown.getMessage());
  }

  @Test
  void parameterValidation_throwsError_withPartialNonexistentParams() {
    Set<String> schemaParams = Set.of("cat");
    Set<String> givenParams = Set.of("cat", "dog");

    ParameterValidationException thrown = assertThrows(ParameterValidationException.class,
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

    assertThrows(ParameterValidationException.class, () -> validateRequestBodyNonexistent(request));
  }

  @Test
  void addEvaluatedDwsParameters_addsEvaluatedJexlParams() {
    Map<String, Object> inputParams = Collections.singletonMap("someParam", "someValue");
    Map<String, String> dwsParameters = Map.of("dwsParam", "request.path()", "url", "env.baseUrl");

    ServerRequest request = Mockito.mock(ServerRequest.class);
    when(request.path()).thenReturn("/path");

    JexlEngine jexlEngine = new JexlBuilder().permissions(JexlPermissions.UNRESTRICTED)
        .create();
    JexlHelper jexlHelper = new JexlHelper(jexlEngine);

    when(environmentProperties.getAllProperties()).thenReturn(Map.of("baseUrl", "https://dotwebstack.org/api"));

    Map<String, Object> result =
        addEvaluatedDwsParameters(inputParams, dwsParameters, request, environmentProperties, jexlHelper);

    assertEquals("someValue", result.get("someParam"));
    assertEquals("/path", result.get("dwsParam"));
    assertEquals("https://dotwebstack.org/api", result.get("url"));
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
