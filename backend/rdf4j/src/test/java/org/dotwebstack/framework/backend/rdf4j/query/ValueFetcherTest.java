package org.dotwebstack.framework.backend.rdf4j.query;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_POSTALCODE_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_POSTALCODE_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_POSTALCODE_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_EXAMPLE_2;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValueFetcherTest {

  @Mock
  private DataFetchingEnvironment environment;

  @Mock
  private NodeShapeRegistry nodeShapeRegistry;

  @Test
  void get_ReturnsConvertedLiteral_ForBuiltInScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_IDENTIFIER_FIELD)
        .path(BREWERY_IDENTIFIER_PATH)
        .build(),nodeShapeRegistry);
    Model model = new ModelBuilder()
        .add(BREWERY_EXAMPLE_1, BREWERY_IDENTIFIER_PATH,
            BREWERY_IDENTIFIER_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BREWERY_IDENTIFIER_EXAMPLE_1.stringValue())));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_ReturnsConvertedLiteral_ForNestedObjectType() {

    NodeShape nodeShape = NodeShape.builder()
        .identifier(BREWERY_ADDRESS_PATH)
        .propertyShapes(ImmutableMap.of("postalCode",
            PropertyShape.builder()
                .name(ADDRESS_POSTALCODE_FIELD)
                .path(ADDRESS_POSTALCODE_PATH)
                .identifier(ADDRESS_SHAPE)
                .build()))
        .build();

    GraphQLObjectType fieldType = GraphQLObjectType.newObject()
        .name("address")
        .field(newFieldDefinition()
            .name("postalCode")
            .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
        .build();

    when(nodeShapeRegistry.get(ADDRESS_SHAPE)).thenReturn(nodeShape);

    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_ADDRESS_FIELD)
        .path(BREWERY_ADDRESS_PATH)
        .identifier(ADDRESS_SHAPE)
        .build();

    ValueFetcher valueFetcher = new ValueFetcher(propertyShape,nodeShapeRegistry);
    Model model = new ModelBuilder()
        .add(BREWERY_EXAMPLE_1, BREWERY_ADDRESS_PATH, BREWERY_ADDRESS_EXAMPLE_1)
        .add(BREWERY_ADDRESS_EXAMPLE_1,ADDRESS_POSTALCODE_PATH,ADDRESS_POSTALCODE_EXAMPLE_1)
        .build();

    when(environment.getFieldType()).thenReturn(fieldType);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result,instanceOf(Map.class));
    assertThat((Map<String,Object>)result, IsMapContaining.hasEntry("postalCode","1234 AC"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_ReturnsList_ForBuiltInListWithScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_OWNERS_FIELD)
        .path(BREWERY_OWNERS_PATH)
        .build(),nodeShapeRegistry);
    Model model = new ModelBuilder()
        .add(BREWERY_EXAMPLE_1, BREWERY_OWNERS_PATH,
            BREWERY_OWNERS_EXAMPLE_1)
        .add(BREWERY_EXAMPLE_1, BREWERY_OWNERS_PATH,
            BREWERY_OWNERS_EXAMPLE_2)
        .build();
    when(environment.getFieldType()).thenReturn(GraphQLList.list(Scalars.GraphQLString));
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result,instanceOf(List.class));
    assertThat((List<String>) result,
        CoreMatchers.hasItems(BREWERY_OWNERS_EXAMPLE_1.stringValue(),BREWERY_OWNERS_EXAMPLE_2.stringValue()));
  }

  @Test
  void get_ReturnsString_ForForeignScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_FOUNDED_FIELD)
        .path(BREWERY_FOUNDED_PATH)
        .build(),nodeShapeRegistry);
    Model model = new ModelBuilder()
        .add(BREWERY_EXAMPLE_1, BREWERY_FOUNDED_PATH, BREWERY_FOUNDED_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(
        CoreScalars.DATETIME);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BREWERY_FOUNDED_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsNull_ForAbsentScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder()
        .name(BREWERY_IDENTIFIER_FIELD)
        .path(BREWERY_IDENTIFIER_PATH)
        .build(),nodeShapeRegistry);
    Model model = new TreeModel();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  void get_ThrowsException_ForNonScalarField() {
    // Arrange
    ValueFetcher valueFetcher = new ValueFetcher(PropertyShape.builder().build(),nodeShapeRegistry);
    Model model = new TreeModel();
    when(environment.getFieldType()).thenReturn(GraphQLObjectType.newObject()
        .name(BREWERY_TYPE)
        .build());
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        valueFetcher.get(environment));
  }

}
