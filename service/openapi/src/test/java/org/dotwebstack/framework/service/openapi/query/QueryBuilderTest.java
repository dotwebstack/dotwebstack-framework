package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.OpenApiConfiguration;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.query.filter.ValueWriter;
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
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

  private static OpenAPI openApi;

  @BeforeAll
  static void init() {
    openApi = TestResources.openApi();
  }

  @ParameterizedTest(name = "{6}")
  @MethodSource("queryBuilderArgs")
  void queryBuilder_returnsExpectedQuery(String path, String queryName, String expectedQuery,
      Map<String, Object> inputParams, MediaType mediaType, String varString, String displayName) {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext(path, queryName);
    Optional<QueryInput> queryInput = new GraphQlQueryBuilder().toQuery(responseSchemaContext, inputParams, mediaType);
    String query = queryInput.map(QueryInput::getQuery)
        .orElseThrow();

    assertEquals(expectedQuery, query);
    if (varString != null) {
      assertEquals(varString, getVariablesString(queryInput.orElseThrow()));
    }
  }

  private String getVariablesString(QueryInput queryInput) {
    Map<?, ?> variables = queryInput.getVariables();
    StringBuilder sb = new StringBuilder();
    ValueWriter.write(variables, sb);
    return sb.toString();
  }

  private static Stream<Arguments> queryBuilderArgs() throws IOException {
    return Stream.of(
        Arguments.arguments("/query1", "query1", loadQuery("query1.txt"), Map.of(), null, null, "valid " + "query"),
        Arguments.arguments("/query3/{query3_param1}", "query3", loadQuery("query3.txt"), Map.of("query3_param1", "v1"),
            null, null, "query with arguments"),
        Arguments.arguments("/query3/{query3_param1}", "query3", loadQuery("query3_exp.txt"),
            Map.of("query3_param1", "v1", X_DWS_EXPANDED_PARAMS, List.of("o2_prop2")), null, null,
            "query with expand arguments"),
        Arguments.arguments("/query5", "query5", loadQuery("query5.txt"), Map.of(), null, null,
            "query with composed root object"),
        Arguments.arguments("/query15", "query5", loadQuery("query15.txt"), Map.of(), null, null,
            "query with composed root object and nested composed object"),
        Arguments.arguments("/query16/{query16_param1}", "query16", loadQuery("query16.txt"), Map.of(), null, null,
            "query with " + "array"),
        Arguments.arguments("/query16/{query16_param1}", "query16", loadQuery("query16_key.txt"),
            Map.of("query16_param1", "id1"), null, null, "query with key parameter"),
        Arguments.arguments("/query16/{query16_param1}", "query16", loadQuery("query16_nested_key.txt"),
            Map.of("query16_param1", "id1", "query16_param2", "id2"), null, null, "query with nested key parameter"),
        Arguments.arguments("/query4", "query4", loadQuery("query4.txt"), Map.of("o3_prop1", "val1"), null,
            loadVariables("query4.txt"), "query with filter"));
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
        () -> graphQlQueryBuilder.toQuery(responseSchemaContext, inputParams, null));
  }

  @Test
  void toQuery_returnsEmptyOptional_forNullQueryName() {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query1", "query1");
    responseSchemaContext.getDwsQuerySettings()
        .setQueryName(null);
    Optional<String> query = new GraphQlQueryBuilder().toQuery(responseSchemaContext, Map.of(), null)
        .map(QueryInput::getQuery);;

    assertTrue(query.isEmpty());
  }

  @Test
  void toQuery_returnsEmptyOptional_forEmptyQueryName() {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query1", "query1");
    responseSchemaContext.getDwsQuerySettings()
        .setQueryName("");
    Optional<String> query = new GraphQlQueryBuilder().toQuery(responseSchemaContext, Map.of(), null)
        .map(QueryInput::getQuery);

    assertTrue(query.isEmpty());
  }

  @Test
  void toQuery_addKey_forPost() throws IOException {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query1", "query1", HttpMethod.POST);
    String query =
        new GraphQlQueryBuilder().toQuery(responseSchemaContext, Map.of("argument1", "id1"), MediaType.APPLICATION_JSON)
            .map(QueryInput::getQuery)
            .orElseThrow();

    assertEquals(loadQuery("query1_body_param.txt"), query);
  }

  private ResponseSchemaContext getResponseSchemaContext(String path, String queryName) {
    return getResponseSchemaContext(path, queryName, HttpMethod.GET);
  }

  private ResponseSchemaContext getResponseSchemaContext(String path, String queryName, HttpMethod method) {
    var responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .xdwsStringTypes(List.of())
        .build();
    var requestBodyContextBuilder = new RequestBodyContextBuilder(openApi);

    Operation operation;
    if (method == HttpMethod.POST) {
      operation = openApi.getPaths()
          .get(path)
          .getPost();
    } else {
      operation = openApi.getPaths()
          .get(path)
          .getGet();
    }
    HttpMethodOperation httpOperation = HttpMethodOperation.builder()
        .name(queryName)
        .operation(operation)
        .httpMethod(method)
        .build();

    return OpenApiConfiguration.buildResponseSchemaContext(httpOperation, responseTemplateBuilder,
        requestBodyContextBuilder);
  }

  private static String loadQuery(String name) throws IOException {
    return IOUtils.toString(QueryBuilderTest.class.getClassLoader()
        .getResourceAsStream("queries/" + name), StandardCharsets.UTF_8);
  }

  private static String loadVariables(String name) throws IOException {
    return IOUtils.toString(QueryBuilderTest.class.getClassLoader()
        .getResourceAsStream("variables/" + name), StandardCharsets.UTF_8);
  }

}
