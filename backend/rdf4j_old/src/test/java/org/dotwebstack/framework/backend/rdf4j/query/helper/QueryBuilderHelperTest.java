package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.query.model.Aggregate;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class QueryBuilderHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final String DWS_BEER_PREFIX = "https://github.com/dotwebstack/beer/def#";

  private static final IRI breweryIri = VF.createIRI(DWS_BEER_PREFIX + "Brewery");

  @Mock
  private Variable subjectMock;

  @Mock
  private Variable objectSubjectMock;

  @Mock
  private RdfObject rdfObjectMock;

  @Test
  public void buildWhereTriples_ReturnsTriple_forSingleValue() {
    // Arrange
    when(subjectMock.getQueryString()).thenReturn("?x0");

    Vertice vertice = Vertice.builder()
        .subject(subjectMock)
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .predicate(() -> stringify(RDF.TYPE))
            .values(Set.of(breweryIri))
            .build()))
        .build();

    // Act
    List<GraphPattern> triples = QueryBuilderHelper.buildWhereTriples(vertice);

    // Assert
    assertThat(triples, hasSize(1));
    assertThat(triples.get(0)
        .getQueryString(),
        is(equalTo(
            "?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Brewery> .")));
  }

  @Test
  public void buildWhereTriples_ReturnsTriple_forSingleRdfObject() {
    // Arrange
    when(subjectMock.getQueryString()).thenReturn("?x0");
    when(rdfObjectMock.getQueryString()).thenReturn("?x12");

    Vertice vertice = Vertice.builder()
        .subject(subjectMock)
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .predicate(() -> stringify(RDF.TYPE))
            .values(Set.of(rdfObjectMock))
            .build()))
        .build();

    // Act
    List<GraphPattern> triples = QueryBuilderHelper.buildWhereTriples(vertice);

    // Assert
    assertThat(triples, hasSize(1));
    assertThat(triples.get(0)
        .getQueryString(), is(equalTo("?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x12 .")));
  }

  @Test
  public void buildWhereTriples_ReturnsTriple_forMultipleValues() {
    // Arrange
    when(subjectMock.getQueryString()).thenReturn("?x0");
    when(rdfObjectMock.getQueryString()).thenReturn("?x12");

    Vertice vertice = Vertice.builder()
        .subject(subjectMock)
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .predicate(() -> stringify(RDF.TYPE))
            .values(Set.of(breweryIri, rdfObjectMock))
            .build()))
        .build();

    // Act
    List<GraphPattern> triples = QueryBuilderHelper.buildWhereTriples(vertice);

    // Assert
    assertThat(triples, hasSize(2));
    String queryString = triples.get(0)
        .getQueryString();
    assertTrue(triples.stream()
        .allMatch(triple -> triple.getQueryString()
            .equals(
                "{ ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Brewery> . }")
            || triple.getQueryString()
                .equals("{ ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x12 . }")));
  }

  @Test
  public void getSubject_ReturnsProvidedSubject_ForEdgeWithoutObject() {
    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .build())
        .build();

    Variable subject = QueryBuilderHelper.getSubject(edge, subjectMock);
    assertThat(subject, is(equalTo(subjectMock)));
  }

  @Test
  public void getSubject_ReturnsObjectSubject_ForEdgeWithObject() {
    Edge edge = Edge.builder()
        .object(Vertice.builder()
            .subject(objectSubjectMock)
            .build())
        .build();

    Variable subject = QueryBuilderHelper.getSubject(edge, subjectMock);
    assertThat(subject, is(not(equalTo(subjectMock))));
    assertThat(subject, is(equalTo(objectSubjectMock)));
  }

  @Test
  public void getSubject_ReturnsAggregateSubject_ForEdgeWithAggregateObject() {
    Edge edge = Edge.builder()
        .aggregate(Aggregate.builder()
            .variable(objectSubjectMock)
            .build())
        .build();

    Variable subject = QueryBuilderHelper.getSubject(edge, subjectMock);
    assertThat(subject, is(not(equalTo(subjectMock))));
    assertThat(subject, is(equalTo(objectSubjectMock)));
  }
}
