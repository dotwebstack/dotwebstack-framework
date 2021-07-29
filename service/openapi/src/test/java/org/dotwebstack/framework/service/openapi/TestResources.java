package org.dotwebstack.framework.service.openapi;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;

public class TestResources {

  private static final String OPEN_API_FILE = "config/model/openapi.yml";

  private static final String GRAPH_QL_FILE = "config/schema.graphqls";

  private static final String OPEN_API_STRING = readString(OPEN_API_FILE);

  private TestResources() {}

  public static OpenAPI openApi() {
    return new OpenAPIV3Parser().readContents(OPEN_API_STRING)
        .getOpenAPI();
  }

  public static InputStream openApiStream() {
    return TestResources.class.getClassLoader()
        .getResourceAsStream(OPEN_API_FILE);
  }

  public static GraphQlField getGraphQlField(TypeDefinitionRegistry typeDefinitionRegistry, String name) {
    ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition) typeDefinitionRegistry.getType("Query")
        .orElseThrow(() -> ExceptionHelper.invalidConfigurationException("Query type not found in graphql schema."));
    FieldDefinition fieldDefinition = objectTypeDefinition.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition1 -> fieldDefinition1.getName()
            .equals(name))
        .findFirst()
        .orElseThrow(() -> ExceptionHelper
            .invalidConfigurationException("Query field definition '{}' not found in graphql schema.", name));
    return new GraphQlFieldBuilder(typeDefinitionRegistry).toGraphQlField(fieldDefinition, new HashMap<>());
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
