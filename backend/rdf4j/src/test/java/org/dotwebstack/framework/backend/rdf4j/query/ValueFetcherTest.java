package org.dotwebstack.framework.backend.rdf4j.query;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_POSTALCODE_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_POSTALCODE_EXAMPLE_2;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_POSTALCODE_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.ADDRESS_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPES_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPE_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPE_EXAMPLE_1_NAME;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPE_EXAMPLE_2;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERTYPE_EXAMPLE_2_NAME;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_BEERTYPE_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_BEERTYPE_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_BEERTYPE_ZERO_OR_MORE_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_BEERTYPE_ZERO_OR_ONE_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_EXAMPLE_2;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_NAME_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_EXAMPLE_2;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_ADDRESS_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERS_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERS_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERTYPE_ZERO_OR_MORE_TYPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERTYPE_ZERO_OR_ONE_TYPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_IDENTIFIER_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_LABEL;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_LABEL_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_NAME_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_NAME_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_EXAMPLE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_EXAMPLE_2;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_OWNERS_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_POSTAL_CODE_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.POSTAL_CODE_1;
import static org.dotwebstack.framework.backend.rdf4j.Constants.POSTAL_CODE_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_ADDRESS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_NAME;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_POSTAL_CODE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.dotwebstack.framework.backend.rdf4j.converters.DateConverter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.AlternativePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPathFactoryTest;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

  @Mock
  private NodeShape nodeShape;

  private List<CoreConverter<?>> converters;

  @BeforeAll
  static void setup() throws IOException {
    PropertyPathFactoryTest.setup();
  }

  @BeforeEach
  void setupTest() {
    when(environment.getParentType()).thenReturn(GraphQLObjectType.newObject()
        .name("testOjbect")
        .build());
    when(environment.getField()).thenReturn(Field.newField()
        .name(BREWERY_IDENTIFIER_FIELD)
        .build());
    when(nodeShapeRegistry.get((GraphQLObjectType) any())).thenReturn(nodeShape);
    this.converters = Arrays.asList(new CoreConverter<?>[] {new DateConverter()});
  }

  @Test
  void get_ReturnsConvertedLiteral_ForBuiltInScalarField() {
    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_IDENTIFIER_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_IDENTIFIER_PATH)
            .build())
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_IDENTIFIER_PATH, BREWERY_IDENTIFIER_EXAMPLE_1)
        .build();

    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BREWERY_IDENTIFIER_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsQuerySolution_ForNestedObjectType() {
    // Arrange
    GraphQLObjectType fieldType = GraphQLObjectType.newObject()
        .name("address")
        .field(newFieldDefinition().name("postalCode")
            .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
        .build();

    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_ADDRESS_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_ADDRESS_PATH)
            .build())
        .identifier(ADDRESS_SHAPE)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_ADDRESS_PATH, BREWERY_ADDRESS_EXAMPLE_1)
        .add(BREWERY_ADDRESS_EXAMPLE_1, ADDRESS_POSTALCODE_PATH, ADDRESS_POSTALCODE_EXAMPLE_1)
        .build();

    when(environment.getFieldType()).thenReturn(fieldType);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, instanceOf(QuerySolution.class));
  }

  @Test
  void get_ReturnsConvertedLiteralList_ForScalarFieldList() {
    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_OWNERS_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_OWNERS_PATH)
            .build())
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_OWNERS_PATH, BREWERY_OWNERS_EXAMPLE_1)
        .add(BREWERY_EXAMPLE_1, BREWERY_OWNERS_PATH, BREWERY_OWNERS_EXAMPLE_2)
        .build();

    when(environment.getFieldType()).thenReturn(GraphQLList.list(GraphQLString));
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, instanceOf(List.class));
    assertThat(((List) result).get(0), equalTo(BREWERY_OWNERS_EXAMPLE_1.stringValue()));
    assertThat(((List) result).get(1), equalTo(BREWERY_OWNERS_EXAMPLE_2.stringValue()));
  }

  @Test
  void get_ReturnsQuerySolutionList_ForNestedObjectTypeList() {
    GraphQLList fieldType = GraphQLList.list(GraphQLObjectType.newObject()
        .name("address")
        .field(newFieldDefinition().name("postalCode")
            .type(GraphQLNonNull.nonNull(Scalars.GraphQLString)))
        .build());

    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_ADDRESS_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_ADDRESS_PATH)
            .build())
        .identifier(ADDRESS_SHAPE)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_ADDRESS_PATH, BREWERY_ADDRESS_EXAMPLE_1)
        .add(BREWERY_EXAMPLE_1, BREWERY_ADDRESS_PATH, BREWERY_ADDRESS_EXAMPLE_2)
        .add(BREWERY_ADDRESS_EXAMPLE_1, ADDRESS_POSTALCODE_PATH, ADDRESS_POSTALCODE_EXAMPLE_1)
        .add(BREWERY_ADDRESS_EXAMPLE_2, ADDRESS_POSTALCODE_PATH, ADDRESS_POSTALCODE_EXAMPLE_2)
        .build();

    when(environment.getFieldType()).thenReturn(fieldType);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, instanceOf(List.class));
    assertThat(((List) result).size(), equalTo(2));
    assertThat(((List) result).get(0), instanceOf(QuerySolution.class));
    assertThat(((List) result).get(1), instanceOf(QuerySolution.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void get_ReturnsList_ForBuiltInListWithScalarField() {
    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_OWNERS_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_OWNERS_PATH)
            .build())
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_OWNERS_PATH, BREWERY_OWNERS_EXAMPLE_1)
        .add(BREWERY_EXAMPLE_1, BREWERY_OWNERS_PATH, BREWERY_OWNERS_EXAMPLE_2)
        .build();
    when(environment.getFieldType()).thenReturn(GraphQLList.list(Scalars.GraphQLString));
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, instanceOf(List.class));
    assertThat((List<String>) result,
        CoreMatchers.hasItems(BREWERY_OWNERS_EXAMPLE_1.stringValue(), BREWERY_OWNERS_EXAMPLE_2.stringValue()));
  }

  @Test
  void get_ReturnsString_ForForeignScalarField() {
    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_FOUNDED_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_FOUNDED_PATH)
            .build())
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_FOUNDED_PATH, BREWERY_FOUNDED_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(CoreScalars.DATETIME);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BREWERY_FOUNDED_EXAMPLE_1.stringValue())));
  }

  @Test
  void get_ReturnsNull_ForAbsentScalarField() {
    // Arrange
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_IDENTIFIER_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_IDENTIFIER_PATH)
            .build())
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new TreeModel();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  void get_ReturnsString_ForSequencePropertyPath() {
    // Arrange
    PropertyPath propertyPath = PropertyPathFactoryTest.createPropertyPath(BREWERY_POSTAL_CODE_SHAPE);
    PropertyShape propertyShape = PropertyShape.builder()
        .name(POSTAL_CODE_FIELD)
        .path(propertyPath)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BREWERY_EXAMPLE_1, SCHEMA_ADDRESS, BREWERY_ADDRESS_EXAMPLE_1)
        .add(BREWERY_ADDRESS_EXAMPLE_1, SCHEMA_POSTAL_CODE, POSTAL_CODE_1)
        .build();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(POSTAL_CODE_1)));
  }

  @Test
  void get_ReturnsString_ForInversePropertyPath() {
    // Arrange
    PropertyPath propertyPath = PropertyPathFactoryTest.createPropertyPath(BREWERY_BEERS_SHAPE);
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BEERS_FIELD)
        .path(propertyPath)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BEER_EXAMPLE_1, BREWERY_BEERS_PATH, BREWERY_EXAMPLE_1)
        .add(BREWERY_EXAMPLE_1, SCHEMA_NAME, BREWERY_NAME_EXAMPLE_1)
        .add(BEER_EXAMPLE_1, SCHEMA_NAME, BEER_NAME_EXAMPLE_1)
        .build();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLID);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BREWERY_EXAMPLE_1));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BEER_NAME_EXAMPLE_1)));
  }

  @Test
  void get_ReturnsString_ForAlternativePropertyPath() {
    // Arrange
    PropertyPath propertyPath = PropertyPathFactoryTest.createPropertyPath(BREWERY_LABEL_PATH);
    assertThat(propertyPath, is(instanceOf(AlternativePath.class)));

    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_NAME_FIELD)
        .path(propertyPath)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model1 = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_LABEL, BREWERY_NAME_EXAMPLE_1)
        .build();

    Model model2 = new ModelBuilder().add(BREWERY_EXAMPLE_1, SCHEMA_NAME, BREWERY_NAME_EXAMPLE_1)
        .build();

    Model model3 = new ModelBuilder().add(BREWERY_EXAMPLE_1, BREWERY_FOUNDED_PATH, BREWERY_FOUNDED_EXAMPLE_1)
        .build();

    // Act
    when(environment.getFieldType()).thenReturn(GraphQLString);
    when(environment.getSource()).thenReturn(new QuerySolution(model1, BREWERY_EXAMPLE_1));
    Object result1 = valueFetcher.get(environment);

    when(environment.getSource()).thenReturn(new QuerySolution(model2, BREWERY_EXAMPLE_1));
    Object result2 = valueFetcher.get(environment);

    when(environment.getSource()).thenReturn(new QuerySolution(model3, BREWERY_EXAMPLE_1));
    Object result3 = valueFetcher.get(environment);

    // Assert
    assertThat(result1, is(equalTo(BREWERY_NAME_EXAMPLE_1.stringValue())));
    assertThat(result2, is(equalTo(BREWERY_NAME_EXAMPLE_1.stringValue())));
    assertThat(result3, is(equalTo(null)));
  }

  @Test
  void get_ReturnsStringList_ForOneOrMorePropertyPath() {
    // Arrange
    PropertyPath propertyPath = PropertyPathFactoryTest.createPropertyPath(BEER_BEERTYPE_SHAPE);
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BEERTYPES_FIELD)
        .path(propertyPath)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BEER_EXAMPLE_2, BEER_BEERTYPE_PATH, BEERTYPE_EXAMPLE_1)
        .add(BEERTYPE_EXAMPLE_1, SCHEMA_NAME, BEERTYPE_EXAMPLE_1_NAME)
        .add(BEER_EXAMPLE_2, BEER_BEERTYPE_PATH, BEERTYPE_EXAMPLE_2)
        .add(BEERTYPE_EXAMPLE_2, SCHEMA_NAME, BEERTYPE_EXAMPLE_2_NAME)
        .build();
    when(environment.getFieldType()).thenReturn(GraphQLList.list(Scalars.GraphQLString));
    when(environment.getSource()).thenReturn(new QuerySolution(model, BEER_EXAMPLE_2));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(Arrays.asList(BEERTYPE_EXAMPLE_1_NAME, BEERTYPE_EXAMPLE_2_NAME))));
  }

  @Test
  void get_ReturnsStringList_ForZeroOrMorePropertyPath() {
    // Arrange
    PropertyPath propertyPath = PropertyPathFactoryTest.createPropertyPath(BEER_BEERTYPE_ZERO_OR_MORE_SHAPE);
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_BEERTYPE_ZERO_OR_MORE_TYPE)
        .path(propertyPath)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BEER_EXAMPLE_2, BEER_BEERTYPE_PATH, BEERTYPE_EXAMPLE_1)
        .add(BEERTYPE_EXAMPLE_1, SCHEMA_NAME, BEERTYPE_EXAMPLE_1_NAME)
        .add(BEER_EXAMPLE_2, BEER_BEERTYPE_PATH, BEERTYPE_EXAMPLE_2)
        .add(BEERTYPE_EXAMPLE_2, SCHEMA_NAME, BEERTYPE_EXAMPLE_2_NAME)
        .build();
    when(environment.getFieldType()).thenReturn(GraphQLList.list(Scalars.GraphQLString));
    when(environment.getSource()).thenReturn(new QuerySolution(model, BEER_EXAMPLE_2));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(Arrays.asList(BEERTYPE_EXAMPLE_1_NAME, BEERTYPE_EXAMPLE_2_NAME))));
  }

  @Test
  void get_ReturnsString_ForZeroOrOnePropertyPath() {
    // Arrange
    PropertyPath propertyPath = PropertyPathFactoryTest.createPropertyPath(BEER_BEERTYPE_ZERO_OR_ONE_SHAPE);
    PropertyShape propertyShape = PropertyShape.builder()
        .name(BREWERY_BEERTYPE_ZERO_OR_ONE_TYPE)
        .path(propertyPath)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(propertyShape);
    ValueFetcher valueFetcher = new ValueFetcher(nodeShapeRegistry, converters);

    Model model = new ModelBuilder().add(BEER_EXAMPLE_2, BEER_BEERTYPE_PATH, BEERTYPE_EXAMPLE_1)
        .add(BEERTYPE_EXAMPLE_1, SCHEMA_NAME, BEERTYPE_EXAMPLE_1_NAME)
        .build();
    when(environment.getFieldType()).thenReturn(Scalars.GraphQLString);
    when(environment.getSource()).thenReturn(new QuerySolution(model, BEER_EXAMPLE_2));

    // Act
    Object result = valueFetcher.get(environment);

    // Assert
    assertThat(result, is(equalTo(BEERTYPE_EXAMPLE_1_NAME)));
  }
}
