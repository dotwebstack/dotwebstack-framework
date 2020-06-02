package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SelectVerticeFactoryTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Mock
  private NodeShape breweryShapeMock;

  @Mock
  private NodeShape beerShapeMock;

  @Mock
  private PropertyShape beersPropertyShapeMock;

  @Mock
  private PropertyShape identifierPropertyShapeMock;

  @Mock
  private OuterQuery<?> outerQueryMock;

  @Mock
  private Variable x1Mock;

  @Test
  public void createRequiredEdges_returnsAddedEdge_forEdgeWithMinCount1() {
    // Arrange
    when(outerQueryMock.var()).thenReturn(x1Mock);
    when(breweryShapeMock.getPropertyShapes()).thenReturn(Map.of("beers", beersPropertyShapeMock));
    when(beersPropertyShapeMock.getMinCount()).thenReturn(1);
    when(beersPropertyShapeMock.getPath()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Brewery_beers"))
        .build());
    when(x1Mock.getQueryString()).thenReturn("?x1");
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    SelectVerticeFactory.addConstraints(vertice, breweryShapeMock, outerQueryMock);
    List<Edge> edges = vertice.getEdges();

    // Assert
    assertThat(edges, hasSize(1));
    assertThat(edges.get(0)
        .getPredicate()
        .getQueryString(), is(equalTo("<https://github.com/dotwebstack/beer/shapes#Brewery_beers>")));
    assertThat(edges.get(0)
        .getObject()
        .getSubject()
        .getQueryString(), is(equalTo("?x1")));
  }

  @Test
  public void createRequiredEdges_returnsNesteEdge_forNestedNodeshape() {
    // Arrange
    when(outerQueryMock.var()).thenReturn(x1Mock);
    when(breweryShapeMock.getPropertyShapes()).thenReturn(Map.of("beers", beersPropertyShapeMock));
    when(beersPropertyShapeMock.getMinCount()).thenReturn(1);
    when(beersPropertyShapeMock.getPath()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Brewery_beers"))
        .build());
    when(beersPropertyShapeMock.getNode()).thenReturn(beerShapeMock);
    when(beerShapeMock.getPropertyShapes()).thenReturn(Map.of("identifier", identifierPropertyShapeMock));
    when(identifierPropertyShapeMock.getMinCount()).thenReturn(1);
    when(identifierPropertyShapeMock.getPath()).thenReturn(PredicatePath.builder()
        .iri(VF.createIRI("https://github.com/dotwebstack/beer/shapes#Beer_identifier"))
        .build());
    when(x1Mock.getQueryString()).thenReturn("?x1");
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    SelectVerticeFactory.addConstraints(vertice, breweryShapeMock, outerQueryMock);
    List<Edge> edges = vertice.getEdges();

    // Assert
    assertThat(edges, hasSize(1));
    assertThat(edges.get(0)
        .getPredicate()
        .getQueryString(), is(equalTo("<https://github.com/dotwebstack/beer/shapes#Brewery_beers>")));
    assertThat(edges.get(0)
        .getObject()
        .getSubject()
        .getQueryString(), is(equalTo("?x1")));

    List<Edge> beersEdges = edges.get(0)
        .getObject()
        .getEdges();
    assertThat(beersEdges, hasSize(1));
    assertThat(beersEdges.get(0)
        .getPredicate()
        .getQueryString(), is(equalTo("<https://github.com/dotwebstack/beer/shapes#Beer_identifier>")));
    assertThat(beersEdges.get(0)
        .getObject()
        .getSubject()
        .getQueryString(), is(equalTo("?x1")));
  }

  @Test
  public void createRequiredEdges_doesNotReturnEdge_forEdgeWithMinCount0() {
    // Arrange
    when(breweryShapeMock.getPropertyShapes()).thenReturn(Map.of("beers", beersPropertyShapeMock));
    when(beersPropertyShapeMock.getMinCount()).thenReturn(0);
    Vertice vertice = Vertice.builder()
        .build();

    // Act
    SelectVerticeFactory.addConstraints(vertice, breweryShapeMock, outerQueryMock);
    List<Edge> edges = vertice.getEdges();

    // Assert
    assertThat(edges, is(empty()));
  }
}
