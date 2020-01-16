package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.FilteredField;
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
  void getFieldDefinitions_returnsEmptyList_forEmptyFieldPaths() {
    String fieldPath = "";
    List<GraphQLFieldDefinition> fieldDefinitions = FieldPathHelper.getFieldDefinitions(BREWERY_TYPE, fieldPath);

    assertEquals(0, fieldDefinitions.size());
  }

  @Test
  void getFieldDefinitions_throwsError_forNonExistingPath() {
    String fieldPath = "beers.nonExistent.name";
    assertThrows(IllegalArgumentException.class, () -> FieldPathHelper.getFieldDefinitions(BREWERY_TYPE, fieldPath));
  }

  @Test
  void getFieldPath_returnsFieldPath_forSortDirective() {
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(BEERS, INGREDIENTS, NAME))
        .build();

    SelectedField selectedField = new FilteredField(fieldPath);

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("sort")
        .value(List.of(Map.of("field", "ingredients.name", "order", "ASC")))
        .type(Scalars.GraphQLString)
        .build();

    FieldPath result = FieldPathHelper.getFieldPath(selectedField, argument, "sort");
    assertEquals(2, result.getFieldDefinitions()
        .size());
    assertEquals("ingredients", result.getFieldDefinitions()
        .get(0)
        .getName());
    assertEquals("name", result.getFieldDefinitions()
        .get(1)
        .getName());
  }

  @Test
  void getFieldPath_returnsFieldPath_forFilterDirectiveObject() {
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(BEERS, INGREDIENTS, NAME))
        .build();

    SelectedField selectedField = new FilteredField(fieldPath);

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("filter")
        .withDirective(GraphQLDirective.newDirective()
            .name("filter")
            .argument(GraphQLArgument.newArgument()
                .name("field")
                .value("ingredients.name")
                .type(Scalars.GraphQLString)
                .build())
            .argument(GraphQLArgument.newArgument()
                .name("value")
                .value("Hop")
                .type(Scalars.GraphQLString)
                .build())
            .build())
        .type(Scalars.GraphQLString)
        .build();

    FieldPath result = FieldPathHelper.getFieldPath(selectedField, argument, "filter");
    assertEquals(2, result.getFieldDefinitions()
        .size());
    assertEquals("ingredients", result.getFieldDefinitions()
        .get(0)
        .getName());
    assertEquals("name", result.getFieldDefinitions()
        .get(1)
        .getName());
  }

  @Test
  void getFieldPath_returnsFieldPath_forFilterDirectiveScalarType() {
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(NAME))
        .build();

    SelectedField selectedField = new FilteredField(fieldPath);

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("filter")
        .withDirective(GraphQLDirective.newDirective()
            .name("filter")
            .argument(GraphQLArgument.newArgument()
                .name("field")
                .value("name")
                .type(Scalars.GraphQLString)
                .build())
            .build())
        .type(Scalars.GraphQLString)
        .build();

    FieldPath result = FieldPathHelper.getFieldPath(selectedField, argument, "filter");
    assertEquals(1, result.getFieldDefinitions()
        .size());
    assertEquals("name", result.getFieldDefinitions()
        .get(0)
        .getName());
  }
}
