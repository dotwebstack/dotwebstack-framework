package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.OpenApiConfiguration;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContextBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import reactor.netty.http.HttpOperations;

@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

  private static OpenAPI openApi;

  @BeforeAll
  static void init(){
    openApi = TestResources.openApi();
  }

  @Test
  void toQuery_returns_validQuery() throws IOException, URISyntaxException {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query1", "query1");

    String query = new GraphQlQueryBuilder().toQuery(responseSchemaContext, Map.of()).orElseThrow();

    String expected = loadQuery("query1.txt");
    assertEquals(expected, query);
  }


  @Test
  void toQuery_returns_validQueryWithArguments() throws IOException, URISyntaxException {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query3/{query3_param1}", "query3");

    String query = new GraphQlQueryBuilder().toQuery(responseSchemaContext, Map.of("query3_param1","v1")).orElseThrow();

    String expected = loadQuery("query3.txt");
    assertEquals(expected, query);
  }

  @Test
  void toQuery_returns_validQueryWithExpandAndArguments() throws IOException, URISyntaxException {
    ResponseSchemaContext responseSchemaContext = getResponseSchemaContext("/query3/{query3_param1}", "query3");

    String query = new GraphQlQueryBuilder().toQuery(responseSchemaContext, Map.of("query3_param1","v1",X_DWS_EXPANDED_PARAMS,List.of("o2_prop2"))).orElseThrow();

    String expected = loadQuery("query3_exp.txt");
    assertEquals(expected, query);
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

  private ResponseSchemaContext getResponseSchemaContext(String path, String queryName) {
    var responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .xdwsStringTypes(List.of())
        .build();
    var requestBodyContextBuilder = new RequestBodyContextBuilder(openApi);

    Operation get = openApi.getPaths().get(path).getGet();
    HttpMethodOperation httpOperation = HttpMethodOperation.builder()
        .name(queryName).operation(get).httpMethod(HttpMethod.GET).build();

    return OpenApiConfiguration.buildResponseSchemaContext(httpOperation, responseTemplateBuilder, requestBodyContextBuilder);
  }

  private String loadQuery(String name) throws IOException, URISyntaxException {
    return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("queries/"+name), StandardCharsets.UTF_8);
  }

}
