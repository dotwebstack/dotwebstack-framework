package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphQlFieldBuilderTest {

  private TypeDefinitionRegistry registry;

  @BeforeEach
  public void setup() {
    this.registry = loadTypeDefinitionRegistry();
  }

  @Test
  void toGraphQlField_throwsException_MissingType() {
    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    FieldDefinition fieldDefinition = getQueryFieldDefinition("brewery");

    assertThrows(InvalidConfigurationException.class, () -> builder.toGraphQlField(fieldDefinition, new HashMap<>()));
  }

  private FieldDefinition getQueryFieldDefinition(String name) {
    ObjectTypeDefinition query = (ObjectTypeDefinition) this.registry.getType("Query")
        .orElseThrow(() -> invalidConfigurationException(""));
    return query.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(name))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException(""));
  }

  private TypeDefinitionRegistry loadTypeDefinitionRegistry() {
    Reader reader = new InputStreamReader(this.getClass()
        .getClassLoader()
        .getResourceAsStream("config/schema.graphqls"));
    return new SchemaParser().parse(reader);
  }
}
