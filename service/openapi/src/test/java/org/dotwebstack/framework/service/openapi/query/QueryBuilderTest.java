package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryBuilderTest {

  private TypeDefinitionRegistry registry;

  @Mock
  private ResponseContext responseContext;

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
    when(responseContext.getGraphQlField()).thenReturn(queryField);

    // Act
    String query = new GraphQlQueryBuilder().toQuery(responseContext, new HashMap<>());

    // Assert
    assertEquals("query Wrapper{brewery{identifier}}", query);
  }

  @Test
  public void toQuery_returns_validQueryWithArguments() {
    // Arrange
    this.registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    FieldDefinition fieldDefinition = getQueryFieldDefinition("brewery");

    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    GraphQlField queryField = builder.toGraphQlField(fieldDefinition);
    when(responseContext.getGraphQlField()).thenReturn(queryField);

    ImmutableMap<String, Object> arguments = ImmutableMap.of("identifier", "1");

    // Act
    String query = new GraphQlQueryBuilder().toQuery(responseContext, arguments);

    // Assert
    assertEquals("query Wrapper($identifier: ID!){brewery(identifier: $identifier){identifier}}", query);
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
        .getResourceAsStream("config/brewery.graphqls"));
    return new SchemaParser().parse(reader);
  }
}
