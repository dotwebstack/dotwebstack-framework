package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.service.openapi.TestConstants.APPLICATION_JSON_HAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.swagger.v3.oas.models.OpenAPI;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.jexl3.JexlBuilder;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

class QueryMapperTest {

  private static OpenAPI openApi;

  private static QueryMapper queryFactory;

  @BeforeAll
  static void beforeAll() {
    var queryArgumentBuilder = new QueryArgumentBuilder(new JexlBuilder().silent(false)
        .strict(true)
        .create());

    openApi = TestResources.openApi("openapi.yaml");
    queryFactory = new QueryMapper(TestResources.graphQlSchema(), queryArgumentBuilder);
  }

  static Stream<Arguments> arguments() {
    return Stream.of(Arguments.of("/breweries", APPLICATION_JSON, Map.of(), "brewery-collection-expanded"),
        Arguments.of("/breweries", APPLICATION_JSON_HAL, Map.of(), "brewery-collection-expanded"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON, Map.of(), "brewery-pageable-collection"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON_HAL, Map.of(), "brewery-pageable-collection"),
        Arguments.of("/breweries-pageable-with-params", APPLICATION_JSON, Map.of("page", "2", "pageSize", "42"),
            "brewery-pageable-collection-with-params"),
        Arguments.of("/breweries-pageable-with-params", APPLICATION_JSON_HAL, Map.of("page", "2", "pageSize", "42"),
            "brewery-pageable-collection-with-params"),
        Arguments.of("/breweries-all-of", APPLICATION_JSON, Map.of(), "brewery-collection"),
        Arguments.of("/breweries-all-of", APPLICATION_JSON_HAL, Map.of(), "brewery-collection"),
        Arguments.of("/breweries-all-of", APPLICATION_JSON, Map.of("x-dws-expand", List.of("postalAddress")),
            "brewery-collection-expanded"),
        Arguments.of("/brewery/{identifier}", APPLICATION_JSON, Map.of("identifier", "foo"), "brewery"),
        Arguments.of("/brewery/{identifier}", APPLICATION_JSON_HAL, Map.of("identifier", "foo"), "brewery"),
        Arguments.of("/brewery/{identifier}", APPLICATION_JSON,
            Map.of("identifier", "foo", "x-dws-expand", List.of("postalAddress")), "brewery-expanded"),
        Arguments.of("/breweries-filter", APPLICATION_JSON_HAL,
            Map.of("name", List.of("breweryname"), "like", "id1", "empcount", 10), "brewery-collection-filter"),
        Arguments.of("/breweries-maybe", APPLICATION_JSON, Map.of(), "brewery-collection-maybe"));
  }

  @ParameterizedTest
  @MethodSource("arguments")
  void map_returnsExpectedQuery_ForConfiguration(String path, MediaType preferredMediaType,
      Map<String, Object> parameters, String query) throws IOException {
    var operation = openApi.getPaths()
        .get(path)
        .getGet();

    var operationRequest = OperationRequest.builder()
        .context(OperationContext.builder()
            .operation(operation)
            .successResponse(MapperUtils.getSuccessResponse(operation))
            .queryProperties(QueryProperties.fromOperation(operation))
            .build())
        .preferredMediaType(preferredMediaType)
        .parameters(parameters)
        .build();

    var executionInput = queryFactory.map(operationRequest);

    assertThat(executionInput.getQuery(), is(equalTo(TestResources.graphQlQuery(query))));
  }

  static Stream<Arguments> argumentsForExceptions() {
    return Stream.of(
        Arguments.of("/breweries-one-of", APPLICATION_JSON, InvalidConfigurationException.class,
            "Unsupported composition construct oneOf / anyOf encountered."),
        Arguments.of("/breweries-any-of", APPLICATION_JSON, InvalidConfigurationException.class,
            "Unsupported composition construct oneOf / anyOf encountered."),
        Arguments.of("/breweries-object-mismatch", APPLICATION_JSON, InvalidConfigurationException.class,
            "Object schema does not match GraphQL field type (found: String)."),
        Arguments.of("/breweries-string-nullability-exception", APPLICATION_JSON, InvalidConfigurationException.class,
            "Nullability of `status` of type StringSchema in response schema is stricter than GraphQL schema."),
        Arguments.of("/breweries-object-nullability-exception", APPLICATION_JSON, InvalidConfigurationException.class,
            "Nullability of `postalAddress` of type ObjectSchema in response schema is stricter than GraphQL schema."),
        Arguments.of("/breweries-wrapped-object-nullability-exception", APPLICATION_JSON,
            InvalidConfigurationException.class,
            "Nullability of `beers` of type ArraySchema in response schema is stricter than GraphQL schema."),
        Arguments.of("/breweries-maybe-array-nullability-exception", APPLICATION_JSON,
            InvalidConfigurationException.class,
            "Nullability of `beersMaybe` of type ArraySchema in response schema is stricter than GraphQL schema."));
  }

  @ParameterizedTest
  @MethodSource("argumentsForExceptions")
  void map_throwsException_ForErrorCases(String path, MediaType preferredMediaType, Class<?> exceptionClass,
      String message) {
    var operation = openApi.getPaths()
        .get(path)
        .getGet();

    var operationRequest = OperationRequest.builder()
        .context(OperationContext.builder()
            .operation(operation)
            .successResponse(MapperUtils.getSuccessResponse(operation))
            .queryProperties(QueryProperties.fromOperation(operation))
            .build())
        .preferredMediaType(preferredMediaType)
        .build();

    var throwable = assertThrows(RuntimeException.class, () -> queryFactory.map(operationRequest));

    assertThat(throwable, is(instanceOf(exceptionClass)));
    assertThat(throwable.getMessage(), is(message));
  }
}
