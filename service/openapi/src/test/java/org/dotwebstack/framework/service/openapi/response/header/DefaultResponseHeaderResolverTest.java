package org.dotwebstack.framework.service.openapi.response.header;

import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getHandleableResponseEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
class DefaultResponseHeaderResolverTest {

  private OpenAPI openApi;

  @Mock
  private EnvironmentProperties environmentProperties;

  private JexlEngine jexlEngine;

  @BeforeEach
  void beforeEach() {
    openApi = TestResources.openApi("openapi.yaml");
    jexlEngine = new JexlBuilder().silent(false)
        .strict(true)
        .create();
  }

  static Stream<Arguments> arguments() {
    return Stream.of(Arguments.of("/breweries", Map.of(), null, Map.of()),
        Arguments.of("/brewery-header-that-does-nothing", Map.of(), null, Map.of()),
        Arguments.of("/breweries-pageable", Map.of("pageSize", 10, "page", 1), Map.of("foo", Map.of("bar", "baz")),
            Map.of("X-Pagination-Limit", "10", "X-Pagination-Page", "1", "X-Forwarded-Host",
                "https://dotwebstack.org/api/forwarded-host", "X-Foo", "bar", "X-Data-Foo", "baz")));
  }

  @ParameterizedTest
  @MethodSource("arguments")
  void accept(String path, Map<String, Object> parameters, Object data, Map<String, String> expectedResponseHeaders) {
    lenient().when(environmentProperties.getAllProperties())
        .thenReturn(Map.of("baseUrl", "https://dotwebstack.org/api"));

    var operationRequest = OperationRequest.builder()
        .context(createOperationContext(path))
        .preferredMediaType(APPLICATION_JSON)
        .parameters(parameters)
        .build();

    var defaultResponseHeaderResolver = new DefaultResponseHeaderResolver(environmentProperties, jexlEngine);

    var httpHeaders = new HttpHeaders();

    defaultResponseHeaderResolver.resolve(operationRequest, data)
        .accept(httpHeaders);

    if (!expectedResponseHeaders.isEmpty()) {
      expectedResponseHeaders.forEach((name, value) -> assertThat(httpHeaders.get(name), is(List.of(value))));
    } else {
      assertThat(httpHeaders.size(), is(0));
    }
  }

  static Stream<Arguments> argumentsForExceptions() {
    return Stream.of(
        Arguments.of("/brewery-unsupported-header", Map.of(),
            "Unsupported header configuration for `unsupported`. Headers should have a scalar schema type"),
        Arguments.of("/brewery-header-no-results", Map.of(),
            "Could not determine value for header `no-results` with expression or default value."));
  }

  @ParameterizedTest
  @MethodSource("argumentsForExceptions")
  void accept_throwsException_ForErrorCases(String path, Map<String, Object> parameters, String message) {
    var operationRequest = OperationRequest.builder()
        .context(createOperationContext(path))
        .preferredMediaType(APPLICATION_JSON)
        .parameters(parameters)
        .build();

    var defaultResponseHeaderResolver = new DefaultResponseHeaderResolver(environmentProperties, jexlEngine);

    var httpHeaders = new HttpHeaders();

    InvalidConfigurationException invalidConfigurationException = assertThrows(InvalidConfigurationException.class,
        () -> defaultResponseHeaderResolver.resolve(operationRequest, null)
            .accept(httpHeaders));

    assertThat(invalidConfigurationException.getMessage(), is(message));
  }

  private OperationContext createOperationContext(String path) {
    var operation = openApi.getPaths()
        .get(path)
        .getGet();

    return OperationContext.builder()
        .operation(operation)
        .responseEntry(getHandleableResponseEntry(operation))
        .queryProperties(QueryProperties.fromOperation(operation))
        .build();
  }
}
