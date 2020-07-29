package org.dotwebstack.framework.backend.rdf4j.query;

import static java.util.Collections.singletonList;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEERS_TARGET_CLASS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BEER_INGREDIENT;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERS_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_LABEL;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_NAME_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_TARGET_CLASS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.INGREDIENTS_NAME_FIELD;
import static org.dotwebstack.framework.backend.rdf4j.Constants.INGREDIENTS_TARGET_CLASS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SHACL_LITERAL;
import static org.dotwebstack.framework.backend.rdf4j.Constants.XSD_STRING;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.dotwebstack.framework.backend.rdf4j.query.helper.ConstraintHelper.buildValueConstraint;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.query.helper.ConstraintHelper;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Filter;
import org.dotwebstack.framework.backend.rdf4j.query.model.FilterRule;
import org.dotwebstack.framework.backend.rdf4j.query.model.OrderBy;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.serializers.LocalDateSerializer;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.serializers.ZonedDateTimeSerializer;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.InversePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class VerticeFactoryTest {

  private static GraphQLFieldDefinition NAME = GraphQLFieldDefinition.newFieldDefinition()
      .name("name")
      .type(Scalars.GraphQLString)
      .build();

  private static GraphQLFieldDefinition NONNULL_NAME = GraphQLFieldDefinition.newFieldDefinition()
      .name("name")
      .type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
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

  @Mock
  private NodeShape nodeShape;

  @Mock
  private Rdf4jProperties rdf4jProperties;

  @Mock
  private Rdf4jProperties.ShapeProperties shapeProperties;

  private SerializerRouter router =
      new SerializerRouter(ImmutableList.of(new LocalDateSerializer(), new ZonedDateTimeSerializer()));

  private VerticeFactory verticeFactory;

  @Mock
  SelectedField selectedField;

  private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

  @Mock
  private SimpleLiteral stringLiteralMock;

  @Mock
  private SimpleLiteral minCountLiteralMock;

  @BeforeEach
  private void setup() {
    verticeFactory = new VerticeFactory(router, rdf4jProperties);
  }

  @Test
  void get_ReturnVertice_ForSimpleBreweryNodeShape() {
    // Arrange
    when(nodeShape.getClasses()).thenReturn(Set.of(Set.of(BREWERY_TARGET_CLASS)));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice =
        verticeFactory.buildSelectQuery(nodeShape, Collections.emptyList(), Collections.emptyList(), query);

    // Assert
    assertThat(vertice.getConstraints()
        .size(), is(1));
    Constraint constraint = vertice.getConstraints()
        .iterator()
        .next();

    assertThat(constraint.getPredicate()
        .getQueryString(), is(stringify(RDF.TYPE)));
    assertThat(constraint.getValues()
        .iterator()
        .next(), is(equalTo(Set.of(BREWERY_TARGET_CLASS))));
  }

  @Test
  void get_ReturnVertice_WithSortedSelectQuery() {
    // Arrange
    PropertyShape breweryName = PropertyShape.builder()
        .name(BREWERY_NAME_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_LABEL)
            .build())
        .nodeKind(SHACL_LITERAL)
        .datatype(XSD_STRING)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(breweryName);
    when(nodeShape.getClasses()).thenReturn(Set.of(Set.of(BREWERY_TARGET_CLASS)));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = verticeFactory.buildSelectQuery(nodeShape, Collections.emptyList(),
        ImmutableList.of(OrderBy.builder()
            .fieldPath(FieldPath.builder()
                .fieldDefinitions(singletonList(NAME))
                .build())
            .order("DESC")
            .build()),
        query);

    // Assert
    assertThat(vertice.getOrderables()
        .size(), is(1));

    assertThat(vertice.getOrderables()
        .get(0)
        .getQueryString(), is("DESC( COALESCE( ?x1, \"\" ) )"));
  }

  @Test
  void get_ReturnVertice_WithSortedOptionalSelectQuery() {
    // Arrange
    PropertyShape breweryName = PropertyShape.builder()
        .name(BREWERY_NAME_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_LABEL)
            .build())
        .nodeKind(SHACL_LITERAL)
        .datatype(XSD_STRING)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(breweryName);
    when(nodeShape.getClasses()).thenReturn(Set.of(Set.of(BREWERY_TARGET_CLASS)));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = verticeFactory.buildSelectQuery(nodeShape, Collections.emptyList(),
        ImmutableList.of(OrderBy.builder()
            .fieldPath(FieldPath.builder()
                .fieldDefinitions(singletonList(NONNULL_NAME))
                .build())
            .order("DESC")
            .build()),
        query);

    // Assert
    assertThat(vertice.getOrderables()
        .size(), is(1));

    assertThat(vertice.getOrderables()
        .get(0)
        .getQueryString(), is("DESC( ?x1 )"));
  }

  @Test
  void get_ReturnVertice_WithFilteredSelectQuery() {
    // Arrange
    when(rdf4jProperties.getShape()).thenReturn(shapeProperties);
    when(shapeProperties.getLanguage()).thenReturn("en");

    PropertyShape ingredientName = PropertyShape.builder()
        .name(INGREDIENTS_NAME_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_LABEL)
            .build())
        .nodeKind(SHACL_LITERAL)
        .datatype(XSD_STRING)
        .build();

    NodeShape ingredientShape = NodeShape.builder()
        .name("Ingredient")
        .propertyShapes(ImmutableMap.of("name", ingredientName))
        .classes(Set.of(Set.of(INGREDIENTS_TARGET_CLASS)))
        .build();

    PropertyShape beerIngredients = PropertyShape.builder()
        .name("ingredients")
        .node(ingredientShape)
        .path(PredicatePath.builder()
            .iri(BEER_INGREDIENT)
            .build())
        .datatype(XSD_STRING)
        .build();

    NodeShape beersShape = NodeShape.builder()
        .name("Beer")
        .propertyShapes(ImmutableMap.of("ingredients", beerIngredients))
        .classes(Set.of(Set.of(BEERS_TARGET_CLASS)))
        .build();

    PropertyShape breweryBeers = PropertyShape.builder()
        .name(BREWERY_BEERS_FIELD)
        .path(InversePath.builder()
            .object(PredicatePath.builder()
                .iri(BREWERY_BEERS)
                .build())
            .build())
        .node(beersShape)
        .build();

    when(nodeShape.getPropertyShape("beers")).thenReturn(breweryBeers);
    when(nodeShape.getChildNodeShape(any())).thenReturn(Optional.of(beersShape));
    when(nodeShape.getClasses()).thenReturn(Set.of(Set.of(BREWERY_TARGET_CLASS)));

    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = verticeFactory.buildSelectQuery(nodeShape, singletonList(FilterRule.builder()
        .fieldPath(FieldPath.builder()
            .fieldDefinitions(Arrays.asList(BEERS, INGREDIENTS, NAME))
            .build())
        .value("Hop")
        .build()), Collections.emptyList(), query);

    // Assert
    assertThat(vertice.getConstraints(), hasSize(1));
    Constraint constraint = vertice.getConstraints()
        .iterator()
        .next();
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));

    assertThat(vertice.getEdges(), hasSize(1));
    Edge edge = vertice.getEdges()
        .get(0);

    assertThat(edge.getObject()
        .getConstraints(), hasSize(1));
    constraint = edge.getObject()
        .getConstraints()
        .iterator()
        .next();
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));

    assertThat(edge.getObject()
        .getEdges(), hasSize(1));
    edge = edge.getObject()
        .getEdges()
        .get(0);

    assertThat(edge.getObject()
        .getConstraints(), hasSize(1));
    constraint = edge.getObject()
        .getConstraints()
        .iterator()
        .next();
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));

    assertThat(edge.getObject()
        .getEdges(), hasSize(1));
    edge = edge.getObject()
        .getEdges()
        .get(0);

    assertThat(edge.getObject()
        .getFilters()
        .size(), is(1));
    Filter filter = edge.getObject()
        .getFilters()
        .get(0);

    assertThat(filter.getOperator(), is(FilterOperator.EQ));
    assertThat(filter.getOperands()
        .size(), is(1));

    Operand operand = filter.getOperands()
        .get(0);
    assertThat(operand.getQueryString(), is("\"Hop\"^^<http://www.w3.org/2001/XMLSchema#string>"));
  }

  @Test
  void get_ReturnVertice_WithNestedFilteredSelectQuery() {
    // Arrange
    when(rdf4jProperties.getShape()).thenReturn(shapeProperties);
    when(shapeProperties.getLanguage()).thenReturn("en");

    PropertyShape breweryName = PropertyShape.builder()
        .name(BREWERY_NAME_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_LABEL)
            .build())
        .nodeKind(SHACL_LITERAL)
        .datatype(XSD_STRING)
        .build();

    when(nodeShape.getPropertyShape(any())).thenReturn(breweryName);
    when(nodeShape.getClasses()).thenReturn(Set.of(Set.of(BREWERY_TARGET_CLASS)));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = verticeFactory.buildSelectQuery(nodeShape, singletonList(FilterRule.builder()
        .fieldPath(FieldPath.builder()
            .fieldDefinitions(singletonList(NAME))
            .build())
        .value("Alfa Brouwerij")
        .build()), Collections.emptyList(), query);

    // Assert
    assertThat(vertice.getConstraints(), hasSize(1));
    Constraint constraint = vertice.getConstraints()
        .iterator()
        .next();
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));

    assertThat(vertice.getEdges(), hasSize(1));
    Edge edge = vertice.getEdges()
        .get(0);

    assertThat(edge.getObject()
        .getFilters()
        .size(), is(1));
    Filter filter = edge.getObject()
        .getFilters()
        .get(0);

    assertThat(filter.getOperator(), is(FilterOperator.EQ));
    assertThat(filter.getOperands()
        .size(), is(1));

    Operand operand = filter.getOperands()
        .get(0);
    assertThat(operand.getQueryString(), is("\"Alfa Brouwerij\"^^<http://www.w3.org/2001/XMLSchema#string>"));
  }

  @Test
  void get_ReturnVertice_WithConstructQuery() {
    // Arrange
    PropertyShape breweryName = PropertyShape.builder()
        .name(BREWERY_NAME_FIELD)
        .path(PredicatePath.builder()
            .iri(BREWERY_LABEL)
            .build())
        .nodeKind(SHACL_LITERAL)
        .datatype(XSD_STRING)
        .build();

    when(nodeShape.getPropertyShape(BREWERY_NAME_FIELD)).thenReturn(breweryName);
    when(nodeShape.getClasses()).thenReturn(Set.of(Set.of(BREWERY_TARGET_CLASS)));
    SelectQuery query = Queries.SELECT();

    when(selectedField.getName()).thenReturn(BREWERY_NAME_FIELD);
    when(selectedField.getQualifiedName()).thenReturn(BREWERY_NAME_FIELD);
    when(selectedField.getFieldDefinition()).thenReturn(mock(GraphQLFieldDefinition.class));
    ImmutableList<SelectedField> fields = ImmutableList.of(selectedField);

    // Act
    Vertice vertice = verticeFactory.buildConstructQuery(nodeShape, fields, query);

    // Assert
    assertThat(vertice.getEdges(), hasSize(2));
    Edge nameEdge = vertice.getEdges()
        .get(0);
    assertThat(nameEdge.getPredicate()
        .getQueryString(), is(stringify(BREWERY_LABEL)));

    Edge typeEdge = vertice.getEdges()
        .get(1);
    assertThat(typeEdge.getPredicate()
        .getQueryString(), is(stringify(RDF.TYPE)));

    assertThat(vertice.getConstraints(), hasSize(1));
    Constraint constraint = vertice.getConstraints()
        .iterator()
        .next();
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));
  }

  @Test
  public void getValueConstraint_retrunsConstraint_forPropertyShape() {
    // Arrange
    when(minCountLiteralMock.intValue()).thenReturn(1);

    PropertyShape propertyShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/dotwebstack/hasCitroenIngredient"))
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, minCountLiteralMock, ConstraintType.HASVALUE, stringLiteralMock))
        .build();

    // Act
    Optional<Constraint> constraintOptional = buildValueConstraint(propertyShape);

    // Assert
    assertTrue(constraintOptional.isPresent());
  }

  @Test
  public void getRequiredEdges_returnsEdge_forPropertyShape() {
    // Arrange
    OuterQuery<?> query = Queries.CONSTRUCT();
    when(minCountLiteralMock.intValue()).thenReturn(1);

    PropertyShape propertyShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/dotwebstack/hasBeers"))
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, minCountLiteralMock))
        .node(NodeShape.builder()
            .name("Beer")
            .propertyShapes(Map.of("identifier", PropertyShape.builder()
                .path(PredicatePath.builder()
                    .iri(VF.createIRI("http://www.example.com/dotwebstack/identifier"))
                    .build())
                .build()))
            .build())
        .build();

    // Act
    List<Edge> requiredEdges = ConstraintHelper.resolveRequiredEdges(List.of(propertyShape), query);

    // Assert
    assertThat(requiredEdges, hasSize(1));
  }
}
