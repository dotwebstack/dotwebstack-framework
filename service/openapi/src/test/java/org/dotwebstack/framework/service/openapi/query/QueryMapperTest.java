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
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.ResourceProperties;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QueryMapperTest {

  private static final String MEDIA_TYPE_JSON = "application/json";

  private static final String MEDIA_TYPE_JSON_HAL = "application/hal+json";

  private static OpenAPI openApi;

  private static QueryMapper queryFactory;

  @BeforeAll
  static void beforeAll() {
    openApi = new OpenAPIV3Parser().read(ResourceProperties.getResourcePath()
        .resolve("dbeerpedia.yaml")
        .getPath());

    queryFactory = new QueryMapper(openApi, loadSchema());
  }

  @ParameterizedTest
  @ValueSource(strings = {MEDIA_TYPE_JSON, MEDIA_TYPE_JSON_HAL})
  void create(String mediaTypeKey) throws IOException {
    var operation = openApi.getPaths()
        .get("/breweries")
        .getGet();

    var operationRequest = OperationRequest.builder()
        .context(OperationContext.builder()
            .operation(operation)
            .successResponse(MapperUtils.getSuccessResponse(operation))
            .build())
        .preferredMediaType(mediaTypeKey)
        .build();

    var executionInput = queryFactory.map(operationRequest);

    assertThat(executionInput.getQuery(), is(equalTo(loadQuery("collection"))));
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
