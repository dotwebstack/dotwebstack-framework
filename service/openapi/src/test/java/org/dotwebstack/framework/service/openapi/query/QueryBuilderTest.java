package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

    StringJoiner bodyJoiner = new StringJoiner(",", "{", "}");
    StringJoiner argumentJoiner = new StringJoiner(",");

    // Act
    new GraphQlQueryBuilder().addToQuery(queryField, new HashSet<>(), bodyJoiner, argumentJoiner, new HashMap<>(), true,
        "");

    // Assert
    assertEquals("{brewery{identifier}}", bodyJoiner.toString());
  }

  @Test
  public void toQuery_returns_validQueryWithArguments() {
    // Arrange
    this.registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    FieldDefinition fieldDefinition = getQueryFieldDefinition("brewery");

    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    GraphQlField queryField = builder.toGraphQlField(fieldDefinition);

    ImmutableMap<String, Object> arguments = ImmutableMap.of("identifier", "1");

    StringJoiner bodyJoiner = new StringJoiner(",", "{", "}");
    StringJoiner argumentJoiner = new StringJoiner(",");

    // Act
    new GraphQlQueryBuilder().addToQuery(queryField, new HashSet<>(), bodyJoiner, argumentJoiner, arguments, true, "");

    // Assert
    assertEquals("$identifier: ID!", argumentJoiner.toString());
    assertEquals("{brewery(identifier: $identifier){identifier}}", bodyJoiner.toString());
  }

  @Test
  public void toQuery_returns_validQueryWithRequiredFieldsAndArguments() {
    // Arrange
    this.registry.add(new ScalarTypeDefinition(CoreScalars.DATETIME.getName()));
    FieldDefinition breweryDefinition = getQueryFieldDefinition("brewery");

    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    GraphQlField breweryField = builder.toGraphQlField(breweryDefinition);

    ImmutableMap<String, Object> arguments = ImmutableMap.of("identifier", "1");
    Set<String> requiredFields = ImmutableSet.of("name", "beers", "beers.name", "beers.ingredients",
        "beers.ingredients.name", "beers.supplements", "beers.supplements.name");
    StringJoiner bodyJoiner = new StringJoiner(",", "{", "}");
    StringJoiner argumentJoiner = new StringJoiner(",");

    // Act
    new GraphQlQueryBuilder().addToQuery(breweryField, requiredFields, bodyJoiner, argumentJoiner, arguments, true, "");

    // Assert
    assertEquals("$identifier: ID!", argumentJoiner.toString());
    assertEquals("{brewery(identifier: $identifier){identifier,name,beers{identifier,name,ingredients{identifier,name},"
        + "supplements{identifier,name}}}}", bodyJoiner.toString());
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
