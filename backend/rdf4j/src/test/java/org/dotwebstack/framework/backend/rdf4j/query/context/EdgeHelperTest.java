package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EdgeHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Test
  void hasEqualTargetClass_returnsTrue_withNullNodeShape() {
    Iri iri = () -> "<http://www.example.com#testType>";

    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> "<" + RDF.TYPE.stringValue() + ">")
                .object(Vertice.builder()
                    .iris(Set.of(iri))
                    .build())
                .build()))
            .build())
        .build();
    PropertyShape shape = PropertyShape.builder()
        .node(NodeShape.builder()
            .targetClasses(Set.of(VF.createIRI("http://www.example.com#testType")))
            .build())
        .build();

    assertTrue(EdgeHelper.hasEqualTargetClass(edge, shape));
  }

  @Test
  void hasEqualTargetClass_returnsFalse_forChildWithoutSameType() {
    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .edges(List.of(Edge.builder()
                .predicate(() -> "<" + RDF.TYPE.stringValue() + ">")
                .object(Vertice.builder()
                    .iris(Collections.emptySet())
                    .build())
                .build()))
            .build())
        .build();
    PropertyShape shape = PropertyShape.builder()
        .node(NodeShape.builder()
            .targetClasses(Set.of(VF.createIRI("http://www.example.com#testType")))
            .build())
        .build();

    assertFalse(EdgeHelper.hasEqualTargetClass(edge, shape));
  }

  @Test
  public void hasEqualTargetClass_returnsTrue_forNullPropertyShape() {
    // Arrange / Act / Assert
    assertTrue(EdgeHelper.hasEqualTargetClass(null, mock(PropertyShape.class)));
  }

  @Test
  public void deepList_returns_listOfEdges() {
    // Arrange
    Edge edge1 = Edge.builder()
        .object(Vertice.builder()
            .edges(Collections.emptyList())
            .build())
        .build();
    Edge edge2 = Edge.builder()
        .object(Vertice.builder()
            .edges(Collections.singletonList(edge1))
            .build())
        .build();
    Edge edge3 = Edge.builder()
        .object(Vertice.builder()
            .edges(Collections.singletonList(edge2))
            .build())
        .build();

    // Act
    List<Edge> edges = EdgeHelper.deepList(Collections.singletonList(edge3));

    // Assert
    assertEquals(3, edges.size());
    assertTrue(edges.contains(edge1));
    assertTrue(edges.contains(edge2));
    assertTrue(edges.contains(edge3));
  }


}
