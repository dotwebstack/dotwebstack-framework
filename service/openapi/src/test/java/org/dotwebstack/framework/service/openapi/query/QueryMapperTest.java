package org.dotwebstack.framework.service.openapi.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.util.ResolverFully;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.ResourceProperties;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

class QueryMapperTest {

  private static final MediaType MEDIA_TYPE_JSON = MediaType.APPLICATION_JSON;

  private static final MediaType MEDIA_TYPE_JSON_HAL = MediaType.valueOf("application/hal+json");

  private static OpenAPI openApi;

  private static QueryMapper queryFactory;

  @BeforeAll
  static void beforeAll() {
    openApi = new OpenAPIV3Parser().read(ResourceProperties.getResourcePath()
        .resolve("dbeerpedia.yaml")
        .getPath());

    new ResolverFully().resolveFully(openApi);

    queryFactory = new QueryMapper(openApi, loadSchema());
  }

  static Stream<Arguments> arguments() {
    return Stream.of(Arguments.of("/breweries", MEDIA_TYPE_JSON, "collection"),
        Arguments.of("/breweries", MEDIA_TYPE_JSON_HAL, "collection"),
        Arguments.of("/breweries-all-of", MEDIA_TYPE_JSON, "collection"),
        Arguments.of("/breweries-all-of", MEDIA_TYPE_JSON_HAL, "collection"));
  }

  @ParameterizedTest
  @MethodSource("arguments")
  void map_returnsExpectedQuery_ForConfiguration(String path, MediaType preferredMediaType, String query)
      throws IOException {
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

    var executionInput = queryFactory.map(operationRequest);

    assertThat(executionInput.getQuery(), is(equalTo(loadQuery(query))));
  }

  static Stream<Arguments> argumentsForExceptions() {
    return Stream.of(
        Arguments.of("/breweries-one-of", MEDIA_TYPE_JSON, InvalidConfigurationException.class,
            "Unsupported composition construct oneOf used."),
        Arguments.of("/breweries-any-of", MEDIA_TYPE_JSON, InvalidConfigurationException.class,
            "Unsupported composition construct anyOf used."));
  }

  @ParameterizedTest
  @MethodSource("argumentsForExceptions")
  void map_throwsExceptions_ForInvalidConfiguration(String path, MediaType preferredMediaType, Class<?> exceptionClass,
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

  private static String loadQuery(String name) throws IOException {
    return IOUtils.toString(Objects.requireNonNull(QueryMapperTest.class.getClassLoader()
        .getResourceAsStream(String.format("queries/%s.graphql", name))), StandardCharsets.UTF_8);
  }

  private static GraphQLSchema loadSchema() {
    var typeDefinitionRegistry = new SchemaParser().parse(Objects.requireNonNull(QueryMapperTest.class.getClassLoader()
        .getResourceAsStream("config/schema.graphql")));

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, RuntimeWiring.MOCKED_WIRING);
  }
}
