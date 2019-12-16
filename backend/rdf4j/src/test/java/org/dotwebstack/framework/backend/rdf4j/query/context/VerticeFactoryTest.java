package org.dotwebstack.framework.backend.rdf4j.query.context;

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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.serializers.LocalDateSerializer;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.serializers.ZonedDateTimeSerializer;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.InversePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Operand;
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

  private SelectVerticeFactory selectVerticeFactory;

  private ConstructVerticeFactory constructVerticeFactory;

  @Mock
  SelectedField selectedField;

  @BeforeEach
  private void setup() {
    selectVerticeFactory = new SelectVerticeFactory(router, rdf4jProperties);
    constructVerticeFactory = new ConstructVerticeFactory(router, rdf4jProperties);
  }

  @Test
  void get_ReturnVertice_ForSimpleBreweryNodeShape() {
    // Arrange
    when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = selectVerticeFactory.createRoot(query.var(), query, nodeShape, Collections.emptyList(),
        Collections.emptyList());

    // Assert
    assertThat(vertice.getEdges()
        .size(), is(1));
    Edge edge = vertice.getEdges()
        .get(0);

    assertThat(edge.getPredicate()
        .getQueryString(), is(stringify(RDF.TYPE)));
    assertThat(edge.getObject()
        .getIris()
        .iterator()
        .next()
        .getQueryString(), is(stringify(BREWERY_TARGET_CLASS)));
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
    when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = selectVerticeFactory.createRoot(query.var(), query, nodeShape, Collections.emptyList(),
        ImmutableList.of(OrderBy.builder()
            .fieldPath(FieldPath.builder()
                .fieldDefinitions(singletonList(NAME))
                .build())
            .order("DESC")
            .build()));

    // Assert
    assertThat(vertice.getOrderables()
        .size(), is(1));

    assertThat(vertice.getOrderables()
        .get(0)
        .getQueryString(), is("DESC( COALESCE( ?x1, \" \" ) )"));
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
    when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = selectVerticeFactory.createRoot(query.var(), query, nodeShape, Collections.emptyList(),
        ImmutableList.of(OrderBy.builder()
            .fieldPath(FieldPath.builder()
                .fieldDefinitions(singletonList(NONNULL_NAME))
                .build())
            .order("DESC")
            .build()));

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
        .targetClasses(Set.of(INGREDIENTS_TARGET_CLASS))
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
        .targetClasses(Set.of(BEERS_TARGET_CLASS))
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
    when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));

    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = selectVerticeFactory.createRoot(query.var(), query, nodeShape, singletonList(FilterRule.builder()
        .fieldPath(FieldPath.builder()
            .fieldDefinitions(Arrays.asList(BEERS, INGREDIENTS, NAME))
            .build())
        .value("Hop")
        .build()), Collections.emptyList());

    // Assert
    assertThat(vertice.getEdges()
        .size(), is(2));
    Edge edge = vertice.getEdges()
        .get(1);

    assertThat(edge.getObject()
        .getEdges()
        .size(), is(2));
    edge = edge.getObject()
        .getEdges()
        .get(1);

    assertThat(edge.getObject()
        .getEdges()
        .size(), is(2));
    edge = edge.getObject()
        .getEdges()
        .get(1);

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
    when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
    SelectQuery query = Queries.SELECT();

    // Act
    Vertice vertice = selectVerticeFactory.createRoot(query.var(), query, nodeShape, singletonList(FilterRule.builder()
        .fieldPath(FieldPath.builder()
            .fieldDefinitions(singletonList(NAME))
            .build())
        .value("Alfa Brouwerij")
        .build()), Collections.emptyList());

    // Assert
    assertThat(vertice.getEdges()
        .size(), is(2));
    Edge edge = vertice.getEdges()
        .get(1);

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
    when(nodeShape.getTargetClasses()).thenReturn(Collections.singleton(BREWERY_TARGET_CLASS));
    SelectQuery query = Queries.SELECT();

    when(selectedField.getName()).thenReturn(BREWERY_NAME_FIELD);
    when(selectedField.getQualifiedName()).thenReturn(BREWERY_NAME_FIELD);
    when(selectedField.getFieldDefinition()).thenReturn(mock(GraphQLFieldDefinition.class));
    ImmutableList<SelectedField> fields = ImmutableList.of(selectedField);

    // Act
    Vertice vertice = constructVerticeFactory.createRoot(query.var(), query, nodeShape, fields);

    // Assert
    assertThat(vertice.getEdges()
        .size(), is(2));
    Edge edge = vertice.getEdges()
        .get(0);

    assertThat(edge.getPredicate()
        .getQueryString(), is(stringify(BREWERY_LABEL)));
  }
}
