package org.dotwebstack.framework.service.openapi;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.util.ResolverFully;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.core.ResourceProperties;

public class TestResources {

  private static final String OPEN_API_FILE = "config/model/openapi.yml";

  private static final String OPEN_API_STRING = readString(OPEN_API_FILE);

  private TestResources() {}

  public static OpenAPI openApi() {
    return new OpenAPIV3Parser().readContents(OPEN_API_STRING)
        .getOpenAPI();
  }

  public static OpenAPI openApi(String name) {
    var openApi = new OpenAPIV3Parser().read(ResourceProperties.getResourcePath()
        .resolve(name)
        .getPath());

    new ResolverFully().resolveFully(openApi);

    return openApi;
  }

  public static InputStream openApiStream() {
    return TestResources.class.getClassLoader()
        .getResourceAsStream(OPEN_API_FILE);
  }

  public static GraphQL graphQl() {
    return GraphQL.newGraphQL(graphQlSchema())
        .build();
  }

  public static GraphQLSchema graphQlSchema() {
    var typeDefinitionRegistry = new SchemaParser().parse(Objects.requireNonNull(TestResources.class.getClassLoader()
        .getResourceAsStream("config/schema.graphql")));

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, RuntimeWiring.MOCKED_WIRING);
  }

  public static String graphQlQuery(String name) throws IOException {
    return IOUtils.toString(Objects.requireNonNull(TestResources.class.getClassLoader()
        .getResourceAsStream(String.format("queries/%s.graphql", name))), StandardCharsets.UTF_8);
  }

  private static String readString(String path) {
    try {
      return FileUtils.readFileToString(new File(TestResources.class.getClassLoader()
          .getResource(path)
          .getFile()), "UTF-8");
    } catch (IOException e) {
      throw new IllegalArgumentException("Resource " + path + "not found.");
    }
  }
}
