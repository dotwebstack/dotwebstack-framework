package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.OpenApiConfiguration;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContextBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

  private static OpenAPI openApi;

  @BeforeAll
  static void init() {
    openApi = TestResources.openApi();
  }

  @ParameterizedTest(name = "{4}")
  @MethodSource("queryBuilderArgs")
  void queryBuilder_returnsExpectedQuery(String path, String queryName, String expectedQuery,
                                         Map<String, Object> inputParams, String displayName) {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext(path, queryName);
    String query = new GraphQlQueryBuilder().toQuery(responseSchemaContext, inputParams)
        .orElseThrow();

    assertEquals(expectedQuery, query);
  }

  private static Stream<Arguments> queryBuilderArgs() throws IOException {
    return Stream.of(Arguments.arguments("/query1", "query1", loadQuery("query1.txt"), Map.of(), "valid query"),
        Arguments.arguments("/query3/{query3_param1}", "query3", loadQuery("query3.txt"), Map.of("query3_param1", "v1"),
            "query with arguments"),
        Arguments.arguments("/query3/{query3_param1}", "query3", loadQuery("query3_exp.txt"),
            Map.of("query3_param1", "v1", X_DWS_EXPANDED_PARAMS, List.of("o2_prop2")), "query with expand arguments"),
        Arguments.arguments("/query5", "query5", loadQuery("query5.txt"), Map.of(), "query with composed root object"),
        Arguments.arguments("/query15", "query5", loadQuery("query15.txt"), Map.of(),
            "query with composed root object and nested composed object"),
        Arguments.arguments("/query16/{query16_param1}", "query16", loadQuery("query16.txt"), Map.of(), "query with " +
            "array"),
        Arguments.arguments("/query16/{query16_param1}", "query16", loadQuery("query16.txt"), Map.of("query16_param1"
            , "id1"), "query with array"));
  }

  @Test
  void validateRequiredPathsQueried_doesNotReturnError_whenRequiredAndQueriedPathsMatch() {
    Set<String> requiredPaths = Set.of("breweries", "beers", "beers.identifier", "beers.name");
    Set<String> queriedPaths = Set.of("beers", "breweries", "beers.name", "beers.identifier");

    assertDoesNotThrow(() -> new GraphQlQueryBuilder().validateRequiredPathsQueried(requiredPaths, queriedPaths));
  }

  @Test
  void validateRequiredPathsQueried_doesNotReturnError_whenRequiredPathsAreQueried() {
    Set<String> requiredPaths = Set.of("beers", "beers.identifier", "beers.name");
    Set<String> queriedPaths = Set.of("beers", "breweries", "beers.name", "beers.identifier");

    assertDoesNotThrow(() -> new GraphQlQueryBuilder().validateRequiredPathsQueried(requiredPaths, queriedPaths));
  }

  @Test
  void validateRequiredPathsQueried_returnsError_whenRequiredPathsAreNotQueried() {
    Set<String> requiredPaths = Set.of("breweries", "beers", "beers.identifier", "beers.name");
    Set<String> queriedPaths = Set.of("beers", "beers.name", "beers.identifier");

    var graphQlQueryBuilder = new GraphQlQueryBuilder();
    assertThrows(InvalidConfigurationException.class,
        () -> graphQlQueryBuilder.validateRequiredPathsQueried(requiredPaths, queriedPaths));
  }

  @Test
  void validate_throwsInvalidConfigurationException_withNoResponseTemplate() {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query14", "query1");

    var graphQlQueryBuilder = new GraphQlQueryBuilder();
    Map<String, Object> inputParams = Map.of();
    assertThrows(InvalidConfigurationException.class,
        () -> graphQlQueryBuilder.toQuery(responseSchemaContext, inputParams));
  }

  private ResponseSchemaContext getResponseSchemaContext(String path, String queryName) {
    var responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .xdwsStringTypes(List.of())
        .build();
    var requestBodyContextBuilder = new RequestBodyContextBuilder(openApi);

    Operation get = openApi.getPaths()
        .get(path)
        .getGet();
    HttpMethodOperation httpOperation = HttpMethodOperation.builder()
        .name(queryName)
        .operation(get)
        .httpMethod(HttpMethod.GET)
        .build();

    return OpenApiConfiguration.buildResponseSchemaContext(httpOperation, responseTemplateBuilder,
        requestBodyContextBuilder);
  }

  private static String loadQuery(String name) throws IOException {
    return IOUtils.toString(QueryBuilderTest.class.getClassLoader()
        .getResourceAsStream("queries/" + name), StandardCharsets.UTF_8);
  }

}
