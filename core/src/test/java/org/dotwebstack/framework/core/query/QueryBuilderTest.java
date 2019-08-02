package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueryBuilderTest {

  private TypeDefinitionRegistry registry;

  @BeforeEach
  public void setup() {
    this.registry = loadTypeDefinitionRegistry();
  }

  @Test
  public void toQuery_returns_validQuery() {
    // Arrange
    this.registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    FieldDefinition fieldDefinition = getQueryFieldDefinition("brewery");

    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    GraphQlField queryField = builder.toGraphQlField(fieldDefinition);

    // Act
    String query = new GraphQlQueryBuilder().toQuery(queryField, new HashMap<>());

    // Assert
    assertEquals("{brewery{identifier,name,founded,foundedAtYear}}", query);
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
