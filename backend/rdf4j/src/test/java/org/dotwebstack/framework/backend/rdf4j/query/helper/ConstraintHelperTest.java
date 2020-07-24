package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.AlternativePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.SequencePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.IntegerLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConstraintHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final String DWS_BEER_PREFIX = "https://github.com/dotwebstack/beer/def#";

  private static final IRI breweryIri = VF.createIRI(DWS_BEER_PREFIX + "Brewery");

  private static final IRI restaurantIri = VF.createIRI(DWS_BEER_PREFIX + "Restaurant");

  private static final IRI beerIri = VF.createIRI(DWS_BEER_PREFIX + "beer");

  private static final IRI identifierIRI = VF.createIRI(DWS_BEER_PREFIX + "identifier");

  @Mock
  private NodeShape beerShapeMock;

  @Mock
  private NodeShape ingredientShapeMock;

  @Mock
  private PropertyShape beersPropertyShapeMock;

  @Mock
  private PropertyShape ingredientPropertyShapeMock;

  @Mock
  private PropertyShape identifierPropertyShapeMock;

  @Mock
  private Variable x1Mock;

  @Mock
  private IntegerLiteral intergerLiteralMock;

  @Mock
  private Value valueMock;

  @Mock
  private Variable variableMock;

  @Mock
  private OuterQuery<?> outerQueryMock;

  @Test
  void hasConstraintOfType_ReturnsFalse_forVerticeWithoutConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Collections.emptySet())
        .build();

    // Act & Assert
    assertFalse(ConstraintHelper.hasConstraintOfType(vertice, Collections.emptySet()));
  }

  @Test
  void hasConstraintOfType_ReturnsFalse_forVerticeWithMinCountConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.MINCOUNT)
            .build()))
        .build();

    // Act & Assert
    assertFalse(ConstraintHelper.hasConstraintOfType(vertice, Collections.emptySet()));
  }

  @Test
  void hasConstraintOfType_ReturnsFalse_forUnmatchingTypeConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .values(Set.of(breweryIri))
            .build()))
        .build();

    // Act & Assert
    assertFalse(ConstraintHelper.hasConstraintOfType(vertice, Set.of(Set.of(restaurantIri))));
  }

  @Test
  void hasConstraintOfType_ReturnsTrue_forMatchingTypeConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .values(Set.of(breweryIri))
            .build()))
        .build();

    // Act & Assert
    assertTrue(ConstraintHelper.hasConstraintOfType(vertice, Set.of(Set.of(breweryIri))));
  }

  @Test
  void buildTypeConstraint_ReturnsEmpty_forNodeShapeWithoutAType() {
    // Arrange
    NodeShape nodeShape = NodeShape.builder()
        .classes(Collections.emptySet())
        .build();

    // Act & Assert
    assertThat(ConstraintHelper.buildTypeConstraint(nodeShape), is(equalTo(Optional.empty())));
  }

  @Test
  void buildTypeConstraint_ReturnsConstraint_forNodeShapeWithSingleType() {
    // Arrange
    NodeShape nodeShape = NodeShape.builder()
        .classes(Set.of(Set.of(breweryIri)))
        .build();

    // Act
    Constraint constraint = ConstraintHelper.buildTypeConstraint(nodeShape)
        .get();

    // Assert
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));
    assertThat(constraint.getValues(), is(equalTo(Set.of(Set.of(breweryIri)))));
    assertThat(constraint.getConstraintType(), is(equalTo(ConstraintType.RDF_TYPE)));
  }

  @Test
  void buildTypeConstraint_ReturnsConstraint_forNodeShapeWithMultipleTypes() {
    // Arrange
    Set<Set<IRI>> classes = Set.of(Set.of(breweryIri, restaurantIri));

    NodeShape nodeShape = NodeShape.builder()
        .classes(classes)
        .build();

    // Act
    Constraint constraint = ConstraintHelper.buildTypeConstraint(nodeShape)
        .get();

    // Assert
    assertThat(constraint.getPredicate()
        .getQueryString(), is(equalTo(stringify(RDF.TYPE))));
    assertThat(constraint.getValues(), is(equalTo(classes)));
    assertThat(constraint.getConstraintType(), is(equalTo(ConstraintType.RDF_TYPE)));
  }

  @Test
  void buildValueConstraint_ReturnsEmpty_forPropertyShapeWithNoMinCount() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(0);

    PropertyShape propertyShape = PropertyShape.builder()
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    // Act & Assert
    assertThat(ConstraintHelper.buildValueConstraint(propertyShape), is(equalTo(Optional.empty())));
  }

  @Test
  void buildValueConstraint_ReturnsEmpty_forPropertyShapeWithMinCount() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);

    PropertyShape propertyShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(identifierIRI)
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock, ConstraintType.HASVALUE, valueMock))
        .build();

    // Act
    Constraint valueConstraint = ConstraintHelper.buildValueConstraint(propertyShape)
        .get();

    // Assert
    assertThat(valueConstraint.getConstraintType(), is(equalTo(ConstraintType.HASVALUE)));
    assertThat(valueConstraint.getValues(), is(equalTo(Set.of(valueMock))));
  }

  @Test
  void addResolvedRequiredEdges_ReturnsNoEdges_forEmptyPropertyShapes() {
    // Act
    List<Edge> edges = ConstraintHelper.resolveRequiredEdges(Collections.emptyList(), outerQueryMock);

    // Assert
    assertTrue(edges.isEmpty());
  }

  @Test
  void addResolvedRequiredEdges_ReturnsNoEdges_forNonRequiredPropertyShapes() {
    // Arrange
    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(identifierIRI)
            .build())
        .constraints(Collections.emptyMap())
        .build();

    // Act
    List<Edge> edges = ConstraintHelper.resolveRequiredEdges(Set.of(identifierShape), outerQueryMock);

    // Assert
    assertTrue(edges.isEmpty());
  }

  @Test
  void addResolvedRequiredEdges_ReturnsMultipleEdges_forMultipleConstrainedPropertyShapes() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);
    when(outerQueryMock.var()).thenReturn(variableMock);

    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(identifierIRI)
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();
    PropertyShape nameShape = PropertyShape.builder()
        .path(AlternativePath.builder()
            .object(SequencePath.builder()
                .first(PredicatePath.builder()
                    .iri(VF.createIRI("http://schema.org/name"))
                    .build())
                .rest(PredicatePath.builder()
                    .iri(VF.createIRI(DWS_BEER_PREFIX + "label"))
                    .build())
                .build())
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    // Act
    List<Edge> edges = ConstraintHelper.resolveRequiredEdges(Set.of(identifierShape, nameShape), outerQueryMock);

    // Assert
    assertThat(edges, hasSize(2));
    assertThat(edges.get(0)
        .getPathTypes(), contains(PathType.CONSTRAINT));
    assertTrue(edges.stream()
        .anyMatch(edge -> edge.getPredicate()
            .getQueryString()
            .equals(stringify(identifierIRI))));

    assertThat(edges.get(1)
        .getPathTypes(), contains(PathType.CONSTRAINT));
    assertTrue(edges.stream()
        .anyMatch(edge -> edge.getPredicate()
            .getQueryString()
            .equals("(<http://schema.org/name>|<https://github.com/dotwebstack/beer/def#label>)")));
  }

  @Test
  void addResolvedRequiredEdges_ReturnsUnchangedVertice_ForNoPropertyShape() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, Collections.emptySet(), outerQueryMock);

    // Assert
    assertTrue(vertice.getEdges()
        .isEmpty());
  }

  @Test
  void addResolvedRequiredEdges_ReturnsUnchangedVertice_ForPropertyShapeWithoutNodeShape() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);

    Vertice vertice = Vertice.builder()
        .build();
    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(identifierIRI)
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, Set.of(identifierShape), outerQueryMock);

    // Assert
    assertTrue(vertice.getEdges()
        .isEmpty());
  }

  @Test
  void addResolvedRequiredEdges_ReturnsVerticeWithExtraEdge_ForPropertyShapeWithBeerNodeShape() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);

    Vertice vertice = Vertice.builder()
        .build();

    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(identifierIRI)
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    PropertyShape beerShape = PropertyShape.builder()
        .path(PredicatePath.builder()
            .iri(beerIri)
            .build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .node(NodeShape.builder()
            .name("Beer")
            .propertyShapes(Map.of("identifier", identifierShape))
            .build())
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, Set.of(beerShape), outerQueryMock);

    // Assert
    assertThat(vertice.getEdges(), hasSize(1));
  }

  @Test
  public void addResolvedRequiredEdges_returnsAddedEdge_forEdgeWithMinCount1() {
    // Arrange
    when(outerQueryMock.var()).thenReturn(x1Mock);
    when(beersPropertyShapeMock.getMinCount()).thenReturn(1);
    when(beersPropertyShapeMock.toPredicate()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Brewery_beers"))
        .build()
        .toPredicate());
    when(beersPropertyShapeMock.getNode()).thenReturn(beerShapeMock);

    when(beerShapeMock.getPropertyShapes()).thenReturn(Map.of("ingredient", ingredientPropertyShapeMock));
    when(ingredientPropertyShapeMock.getMinCount()).thenReturn(1);
    when(ingredientPropertyShapeMock.getNode()).thenReturn(ingredientShapeMock);
    when(ingredientPropertyShapeMock.toPredicate()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Beer_ingredients"))
        .build()
        .toPredicate());

    when(x1Mock.getQueryString()).thenReturn("?x1");
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, List.of(beersPropertyShapeMock), outerQueryMock);
    List<Edge> edges = vertice.getEdges();

    // Assert
    assertThat(edges, hasSize(1));
    assertThat(edges.get(0)
        .getPredicate()
        .getQueryString(), is(Matchers.equalTo("<https://github.com/dotwebstack/beer/shapes#Brewery_beers>")));
    assertThat(edges.get(0)
        .getObject()
        .getSubject()
        .getQueryString(), is(Matchers.equalTo("?x1")));
  }

  @Test
  public void addResolvedRequiredEdges_returnsNesteEdge_forNestedNodeshape() {
    // Arrange
    when(outerQueryMock.var()).thenReturn(x1Mock);
    when(beersPropertyShapeMock.getMinCount()).thenReturn(1);
    when(beersPropertyShapeMock.toPredicate()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Brewery_beers"))
        .build()
        .toPredicate());
    when(beersPropertyShapeMock.getNode()).thenReturn(beerShapeMock);

    when(beerShapeMock.getPropertyShapes()).thenReturn(Map.of("ingredient", ingredientPropertyShapeMock));
    when(ingredientPropertyShapeMock.getMinCount()).thenReturn(1);
    when(ingredientPropertyShapeMock.getNode()).thenReturn(ingredientShapeMock);
    when(ingredientPropertyShapeMock.toPredicate()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Beer_ingredients"))
        .build()
        .toPredicate());
    when(ingredientShapeMock.getPropertyShapes()).thenReturn(Map.of("identifier", identifierPropertyShapeMock));

    when(x1Mock.getQueryString()).thenReturn("?x1");
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, List.of(beersPropertyShapeMock), outerQueryMock);
    List<Edge> edges = vertice.getEdges();

    // Assert
    assertThat(edges, hasSize(1));
    assertThat(edges.get(0)
        .getPredicate()
        .getQueryString(), is(Matchers.equalTo("<https://github.com/dotwebstack/beer/shapes#Brewery_beers>")));
    assertThat(edges.get(0)
        .getObject()
        .getSubject()
        .getQueryString(), is(Matchers.equalTo("?x1")));

    List<Edge> beersEdges = edges.get(0)
        .getObject()
        .getEdges();
    assertThat(beersEdges, hasSize(1));
    assertThat(beersEdges.get(0)
        .getPredicate()
        .getQueryString(), is(Matchers.equalTo("<https://github.com/dotwebstack/beer/shapes#Beer_ingredients>")));
    assertThat(beersEdges.get(0)
        .getObject()
        .getSubject()
        .getQueryString(), is(Matchers.equalTo("?x1")));
  }

  @Test
  public void addResolvedRequiredEdges_doesNotReturnEdge_forEdgeWithMinCount0() {
    // Arrange
    when(beersPropertyShapeMock.getMinCount()).thenReturn(0);
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, List.of(beersPropertyShapeMock), outerQueryMock);
    List<Edge> edges = vertice.getEdges();

    // Assert
    assertThat(edges, is(empty()));
  }

  @Test
  void buildConstraints_ReturnsUnchangedVertice_ForVerticeWithouConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .nodeShape(NodeShape.builder()
            .build())
        .build();

    // Act
    ConstraintHelper.buildConstraints(vertice, outerQueryMock);

    // Assert
    assertTrue(vertice.getConstraints()
        .isEmpty());
  }

  @Test
  void buildConstraints_ReturnsVerticeWithConstraints_ForConstrainedNodeShape() {
    // Arrange
    when(outerQueryMock.var()).thenReturn(variableMock);

    NodeShape beerShape = NodeShape.builder()
        .name("Beer")
        .propertyShapes(Collections.emptyMap())
        .classes(Set.of(Set.of(beerIri)))
        .build();

    NodeShape breweryShape = NodeShape.builder()
        .name("Brewery")
        .propertyShapes(Collections.emptyMap())
        .classes(Set.of(Set.of(breweryIri)))
        .build();
    Vertice vertice = Vertice.builder()
        .edges(List.of(Edge.builder()
            .propertyShape(PropertyShape.builder()
                .build())
            .predicate(() -> stringify(RDF.TYPE))
            .object(Vertice.builder()
                .nodeShape(beerShape)
                .build())
            .build()))
        .nodeShape(breweryShape)
        .build();

    // Act
    ConstraintHelper.buildConstraints(vertice, outerQueryMock);

    // Assert
    assertThat(vertice.getConstraints(), hasSize(1));
    assertThat(vertice.getEdges()
        .get(0)
        .getObject()
        .getConstraints(), hasSize(1));
  }
}
