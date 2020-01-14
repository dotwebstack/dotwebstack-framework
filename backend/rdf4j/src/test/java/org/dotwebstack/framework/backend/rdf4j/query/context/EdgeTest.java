package org.dotwebstack.framework.backend.rdf4j.query.context;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EdgeTest {

  @Test
  public void graphContainsFilter_returnsTrue_for_edgeWithFilter() {
    // Arrange
    Filter filter = mock(Filter.class);
    Vertice vertice = Vertice.builder()
        .filters(Collections.singletonList(filter))
        .build();
    Edge edge = Edge.builder()
        .object(vertice)
        .build();

    // Act / Assert
    assertTrue(edge.graphContainsFilter());
  }

  @Test
  public void graphContainsFilter_returnsTrue_for_edgeWithSubFilter() {
    // Arrange
    Filter filter = mock(Filter.class);
    Vertice subVertice1 = Vertice.builder()
        .filters(Collections.singletonList(filter))
        .build();
    Vertice subVertice2 = Vertice.builder()
        .filters(Collections.emptyList())
        .build();
    Edge subEdge1 = Edge.builder()
        .object(subVertice1)
        .build();
    Edge subEdge2 = Edge.builder()
        .object(subVertice2)
        .build();
    Vertice vertice = Vertice.builder()
        .edges(asList(subEdge1, subEdge2))
        .build();

    Edge edge = Edge.builder()
        .object(vertice)
        .build();

    // Act / Assert
    assertTrue(edge.graphContainsFilter());
  }

  @Test
  public void graphContainsFilter_returnsFalse_for_edgeWithoutFilters() {
    // Arrange
    Vertice subVertice1 = Vertice.builder()
        .filters(Collections.emptyList())
        .build();
    Vertice subVertice2 = Vertice.builder()
        .filters(Collections.emptyList())
        .build();
    Edge subEdge1 = Edge.builder()
        .object(subVertice1)
        .build();
    Edge subEdge2 = Edge.builder()
        .object(subVertice2)
        .build();
    Vertice vertice = Vertice.builder()
        .edges(asList(subEdge1, subEdge2))
        .build();

    Edge edge = Edge.builder()
        .object(vertice)
        .build();

    // Act / Assert
    assertFalse(edge.graphContainsFilter());
  }
}
