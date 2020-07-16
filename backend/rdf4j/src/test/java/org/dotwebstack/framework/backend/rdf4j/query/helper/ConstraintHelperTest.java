package org.dotwebstack.framework.backend.rdf4j.query.helper;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.support.hierarchical.Node;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConstraintHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final String DWS_BEER_PREFIX = "https://github.com/dotwebstack/beer/def#";

  private static final IRI breweryIri = VF.createIRI(DWS_BEER_PREFIX + "Brewery");

  private static final IRI restaurantIri = VF.createIRI(DWS_BEER_PREFIX + "Restaurant");

  private static final IRI beerIri = VF.createIRI(DWS_BEER_PREFIX + "beer");

  private static final IRI identifierIRI = VF.createIRI(DWS_BEER_PREFIX + "identifier");

  @Mock
  private IntegerLiteral intergerLiteralMock;

  @Mock
  private Value valueMock;

  @Mock
  private Variable variableMock;

  @Mock
  private OuterQuery<?> outerQueryMock;

  @Test
  public void hasConstraintOfType_ReturnsFalse_forVerticeWithoutConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Collections.emptySet())
        .build();

    // Act & Assert
    assertFalse(ConstraintHelper.hasConstraintOfType(vertice, any()));
  }

  @Test
  public void hasConstraintOfType_ReturnsFalse_forVerticeWithMinCountConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.MINCOUNT)
            .build()))
        .build();

    // Act & Assert
    assertFalse(ConstraintHelper.hasConstraintOfType(vertice, any()));
  }

  @Test
  public void hasConstraintOfType_ReturnsFalse_forUnmatchingTypeConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .values(Set.of(breweryIri))
            .build()))
        .build();

    // Act & Assert
    assertFalse(ConstraintHelper.hasConstraintOfType(vertice, Set.of(restaurantIri)));
  }

  @Test
  public void hasConstraintOfType_ReturnsTrue_forMatchingTypeConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .values(Set.of(breweryIri))
            .build()))
        .build();

    // Act & Assert
    assertTrue(ConstraintHelper.hasConstraintOfType(vertice, Set.of(breweryIri)));
  }

  @Test
  public void buildTypeConstraint_ReturnsEmpty_forNodeShapeWithoutAType() {
    // Arrange
    NodeShape nodeShape = NodeShape.builder()
        .classes(Collections.emptySet())
        .build();

    // Act & Assert
    assertThat(ConstraintHelper.buildTypeConstraint(nodeShape), is(equalTo(Optional.empty())));
  }

  @Test
  public void buildTypeConstraint_ReturnsConstraint_forNodeShapeWithSingleType() {
    // Arrange
    NodeShape nodeShape = NodeShape.builder()
        .classes(Set.of(breweryIri))
        .build();

    // Act
    Constraint constraint = ConstraintHelper.buildTypeConstraint(nodeShape).get();

    // Assert
    assertThat(constraint.getPredicate().getQueryString(), is(equalTo(stringify(RDF.TYPE))));
    assertThat(constraint.getValues(), is(equalTo(Set.of(breweryIri))));
    assertThat(constraint.getConstraintType(), is(equalTo(ConstraintType.RDF_TYPE)));
  }

  @Test
  public void buildTypeConstraint_ReturnsConstraint_forNodeShapeWithMultipleTypes() {
    // Arrange
    NodeShape nodeShape = NodeShape.builder()
        .classes(Set.of(breweryIri, restaurantIri))
        .build();

    // Act
    Constraint constraint = ConstraintHelper.buildTypeConstraint(nodeShape).get();

    // Assert
    assertThat(constraint.getPredicate().getQueryString(), is(equalTo(stringify(RDF.TYPE))));
    assertThat(constraint.getValues(), is(equalTo(Set.of(breweryIri, restaurantIri))));
    assertThat(constraint.getConstraintType(), is(equalTo(ConstraintType.RDF_TYPE)));
  }

  @Test
  public void buildValueConstraint_ReturnsEmpty_forPropertyShapeWithNoMinCount() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(0);

    PropertyShape propertyShape = PropertyShape.builder()
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    // Act & Assert
    assertThat(ConstraintHelper.buildValueConstraint(propertyShape), is(equalTo(Optional.empty())));
  }

  @Test
  public void buildValueConstraint_ReturnsEmpty_forPropertyShapeWithMinCount() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);

    PropertyShape propertyShape = PropertyShape.builder()
        .path(PredicatePath.builder().iri(identifierIRI).build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock, ConstraintType.HASVALUE, valueMock))
        .build();

    // Act
    Constraint valueConstraint = ConstraintHelper.buildValueConstraint(propertyShape).get();

    // Assert
    assertThat(valueConstraint.getConstraintType(), is(equalTo(ConstraintType.HASVALUE)));
    assertThat(valueConstraint.getValues(), is(equalTo(Set.of(valueMock))));
  }

  @Test
  public void addResolvedRequiredEdges_ReturnsNoEdges_forEmptyPropertyShapes() {
    // Act
    List<Edge> edges = ConstraintHelper.resolveRequiredEdges(Collections.emptyList(), outerQueryMock);

    // Assert
    assertTrue(edges.isEmpty());
  }

  @Test
  public void addResolvedRequiredEdges_ReturnsNoEdges_forNonRequiredPropertyShapes() {
    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder().iri(identifierIRI).build())
        .constraints(Collections.emptyMap())
        .build();

    // Act
    List<Edge> edges = ConstraintHelper.resolveRequiredEdges(Set.of(identifierShape), outerQueryMock);

    // Assert
    assertTrue(edges.isEmpty());
  }

  @Test
  public void addResolvedRequiredEdges_ReturnsMultipleEdges_forMultipleConstrainedPropertyShapes() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);
    when(outerQueryMock.var()).thenReturn(variableMock);

    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder().iri(identifierIRI).build())
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
    assertThat(edges.get(0).getPathTypes(), contains(PathType.CONSTRAINT));
    assertThat(edges.get(0).getPredicate().getQueryString(), is(equalTo(stringify(identifierIRI))));

    assertThat(edges.get(1).getPathTypes(), contains(PathType.CONSTRAINT));
    assertThat(edges.get(1).getPredicate().getQueryString(), is(equalTo("(<http://schema.org/name>|<https://github.com/dotwebstack/beer/def#label>)")));
  }

  @Test
  public void addResolvedRequiredEdges_ReturnsUnchangedVertice_ForNoPropertyShape() {
    // Arrange
    Vertice vertice = Vertice.builder().build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, Collections.emptySet(), outerQueryMock);

    // Assert
    assertTrue(vertice.getEdges().isEmpty());
  }

  @Test
  public void addResolvedRequiredEdges_ReturnsUnchangedVertice_ForPropertyShapeWithoutNodeShape() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);

    Vertice vertice = Vertice.builder().build();
    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder().iri(identifierIRI).build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, Set.of(identifierShape), outerQueryMock);

    // Assert
    assertTrue(vertice.getEdges().isEmpty());
  }

  @Test
  public void addResolvedRequiredEdges_ReturnsVerticeWithExtraEdge_ForPropertyShapeWithBeerNodeShape() {
    // Arrange
    when(intergerLiteralMock.intValue()).thenReturn(1);

    Vertice vertice = Vertice.builder().build();

    PropertyShape identifierShape = PropertyShape.builder()
        .path(PredicatePath.builder().iri(identifierIRI).build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .build();

    PropertyShape beerShape = PropertyShape.builder()
        .path(PredicatePath.builder().iri(beerIri).build())
        .constraints(Map.of(ConstraintType.MINCOUNT, intergerLiteralMock))
        .node(NodeShape.builder().name("Beer").propertyShapes(Map.of("identifier", identifierShape)).build())
        .build();

    // Act
    ConstraintHelper.addResolvedRequiredEdges(vertice, Set.of(beerShape), outerQueryMock);

    // Assert
    assertThat(vertice.getEdges(), hasSize(1));
  }

  @Test
  public void buildConstraints_ReturnsUnchangedVertice_ForVerticeWithouConstraint() {
    // Arrange
    Vertice vertice = Vertice.builder().nodeShape(NodeShape.builder().build()).build();

    // Act
    ConstraintHelper.buildConstraints(vertice, outerQueryMock);

    // Assert
    assertTrue(vertice.getConstraints().isEmpty());
  }

  @Test
  public void buildConstraints_ReturnsVerticeWithConstraints_ForConstrainedNodeShape() {
    // Arrange
    NodeShape beerShape = NodeShape.builder()
        .name("Beer")
        .propertyShapes(Collections.emptyMap())
        .classes(Set.of(beerIri))
        .build();

    NodeShape breweryShape = NodeShape.builder()
        .name("Brewery")
        .propertyShapes(Collections.emptyMap())
        .classes(Set.of(breweryIri))
        .build();
    Vertice vertice = Vertice.builder()
            .edges(List.of(Edge.builder()
                .propertyShape(PropertyShape.builder().build())
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
    assertThat(vertice.getEdges().get(0).getObject().getConstraints(), hasSize(1));
  }
}