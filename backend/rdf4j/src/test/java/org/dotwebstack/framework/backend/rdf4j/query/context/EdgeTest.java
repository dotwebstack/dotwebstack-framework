package org.dotwebstack.framework.backend.rdf4j.query.context;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
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
  public void toString_returnsValidStringRepresentation_ofEdgeWithIri() {
    Iri iri = () -> "<http://www.example.com#testType>";

    Edge edge = Edge.builder()
        .predicate(() -> "<" + RDF.TYPE.stringValue() + ">")
        .object(Vertice.builder()
            .iris(Set.of(iri))
            .build())
        .build();

    assertThat(edge.toString(),
        is(equalTo("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.example.com#testType>")));
  }

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
