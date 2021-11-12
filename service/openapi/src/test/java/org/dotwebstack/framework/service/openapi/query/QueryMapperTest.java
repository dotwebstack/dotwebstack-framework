package org.dotwebstack.framework.service.openapi.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
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

    queryFactory = new QueryMapper(openApi, loadSchema());
  }

  static Stream<Arguments> arguments() {
    return Stream.of(
        Arguments.of("/breweries", MEDIA_TYPE_JSON, "collection"),
        Arguments.of("/breweries", MEDIA_TYPE_JSON_HAL, "collection"));
  }

  @ParameterizedTest
  @MethodSource("arguments")
  void create(String path, MediaType preferredMediaType, String query) throws IOException {
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
