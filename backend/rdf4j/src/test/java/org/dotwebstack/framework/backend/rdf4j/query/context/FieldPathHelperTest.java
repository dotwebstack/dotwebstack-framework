package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import org.junit.jupiter.api.Test;

class FieldPathHelperTest {

  private static GraphQLFieldDefinition NAME = GraphQLFieldDefinition.newFieldDefinition()
      .name("name")
      .type(Scalars.GraphQLString)
      .build();

  private static GraphQLObjectType INGREDIENTS_TYPE = GraphQLObjectType.newObject()
      .name("Ingredient")
      .field(NAME)
      .build();

  private static GraphQLFieldDefinition INGREDIENTS = GraphQLFieldDefinition.newFieldDefinition()
      .name("ingredients")
      .type(INGREDIENTS_TYPE)
      .build();

  private static GraphQLObjectType BEERS_TYPE = GraphQLObjectType.newObject()
      .name("Beer")
      .field(NAME)
      .field(INGREDIENTS)
      .build();

  private static GraphQLFieldDefinition BEERS = GraphQLFieldDefinition.newFieldDefinition()
      .name("beers")
      .type(BEERS_TYPE)
      .build();

  private static GraphQLObjectType BREWERY_TYPE = GraphQLObjectType.newObject()
      .name("Brewery")
      .field(BEERS)
      .field(NAME)
      .build();

  @Test
  void getFieldDefinitions_returnsFieldDefinitions_forIngredientName() {
    String fieldPath = "beers.ingredients.name";
    List<GraphQLFieldDefinition> fieldDefinitions = FieldPathHelper.getFieldDefinitions(BREWERY_TYPE, fieldPath);

    assertEquals(3, fieldDefinitions.size());
    assertEquals("beers", fieldDefinitions.get(0)
        .getName());
    assertEquals("ingredients", fieldDefinitions.get(1)
        .getName());
    assertEquals("name", fieldDefinitions.get(2)
        .getName());
  }

  @Test
  void getFieldDefinitions_throwsError_forNonExistingPath() {
    String fieldPath = "beers.nonExistent.name";
    assertThrows(IllegalArgumentException.class, () -> FieldPathHelper.getFieldDefinitions(BREWERY_TYPE, fieldPath));
  }
}
