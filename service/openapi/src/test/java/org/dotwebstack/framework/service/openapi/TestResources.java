package org.dotwebstack.framework.service.openapi;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.commons.io.FileUtils;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;


public class TestResources {
  private TestResources() {}

  public static OpenAPI openApi() {
    return new OpenAPIV3Parser().read("config/model/openapi.yml");
  }

  public static TypeDefinitionRegistry typeDefinitionRegistry() {
    Reader reader = new InputStreamReader(TestResources.class.getClassLoader()
        .getResourceAsStream("config/schema.graphqls"));
    return new SchemaParser().parse(reader);
  }

  public static TypeDefinitionRegistry typeDefinitionRegistry(String regex, String replacement) throws IOException {
    String schemaString = FileUtils.readFileToString(new File(TestResources.class.getClassLoader()
        .getResource("config/schema.graphqls")
        .getFile()), "UTF-8")
        .replaceAll(regex, replacement);
    return new SchemaParser().parse(schemaString);
  }

  public static QueryFieldHelper queryFieldHelper(TypeDefinitionRegistry registry) {
    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(registry);
    return QueryFieldHelper.builder()
        .typeDefinitionRegistry(registry)
        .graphQlFieldBuilder(builder)
        .build();
  }
}
