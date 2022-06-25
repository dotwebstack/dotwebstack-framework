package org.dotwebstack.framework.ext.rml.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.zalando.problem.jackson.ProblemModule;

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
      throw illegalArgumentException("Resource {} not found.", path);
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

  public static Jackson2ObjectMapperBuilder objectMapperBuilder() {
    var builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .modules(List.of(new JavaTimeModule(), new ProblemModule()));

    return builder;
  }
}
