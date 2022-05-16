package org.dotwebstack.framework.ext.rml.mapping;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

class TestResources {

  static OpenAPI openApi(String path) {
    return new OpenAPIV3Parser().readContents(readString(path))
        .getOpenAPI();
  }

  private static String readString(String path) {
    try {
      return FileUtils.readFileToString(new File(RmlOpenApiConfigurationTest.class.getClassLoader()
          .getResource(path)
          .getFile()), "UTF-8");
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Resource %s not found.", path));
    }
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
}
