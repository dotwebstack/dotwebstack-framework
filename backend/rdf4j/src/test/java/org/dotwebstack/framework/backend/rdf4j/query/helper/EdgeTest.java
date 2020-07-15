package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EdgeTest {

  @Mock
  private Vertice mockVertice;

  @Mock
  private Variable mockVariable;

  @Test
  public void toString_returnsValidStringRepresentation_ofEdgeWithSubject() {
    when(mockVertice.getSubject()).thenReturn(mockVariable);
    when(mockVariable.getQueryString()).thenReturn("?x1");

    Edge edge = Edge.builder()
        .predicate(() -> "beer_def:brewery")
        .object(mockVertice)
        .build();

    assertThat(edge.toString(), is(equalTo("beer_def:brewery ?x1")));
  }

}
