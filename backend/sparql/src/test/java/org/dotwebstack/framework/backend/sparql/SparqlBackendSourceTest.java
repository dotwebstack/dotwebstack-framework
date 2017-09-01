package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendSourceTest {

  private static final String GRAPH_QUERY = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o}";

  private static final String TUPLE_QUERY = "SELECT { ?s ?p ?o } WHERE { ?s ?p ?o}";

  private static final String BOOLEAN_QUERY = "ASK { ?s ?p ?o}";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private SparqlBackend backend;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Before
  public void setUp() {
    when(backend.getConnection()).thenReturn(repositoryConnection);
  }

  @Test
  public void builder() {
    // Act
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, GRAPH_QUERY).build();

    // Assert
    assertThat(backendSource.getBackend(), equalTo(backend));
    assertThat(backendSource.getQuery(), equalTo(GRAPH_QUERY));
  }

  @Test
  public void requiredBackgroundReference() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(null, GRAPH_QUERY).build();
  }

  @Test
  public void requiredQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(null, GRAPH_QUERY).build();
  }

  @Test
  public void getGraphQueryResult() {
    // Arrange
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, GRAPH_QUERY).build();
    GraphQuery query = mock(GraphQuery.class);
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL,
        backendSource.getQuery())).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    // Act
    Object result = backendSource.getResult();

    // Assert
    assertThat(result, instanceOf(GraphQueryResult.class));
  }

  @Test
  public void getTupleQueryResult() {
    // Arrange
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, TUPLE_QUERY).build();
    TupleQuery query = mock(TupleQuery.class);
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL,
        backendSource.getQuery())).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    // Act
    Object result = backendSource.getResult();

    // Assert
    assertThat(result, instanceOf(TupleQueryResult.class));
  }

  @Test
  public void getResultForUnsupportedQueryType() {
    // Arrange
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, BOOLEAN_QUERY).build();
    BooleanQuery query = mock(BooleanQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL,
        backendSource.getQuery())).thenReturn(query);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query type '%s' not supported.", query.getClass()));

    // Act
    backendSource.getResult();
  }

  @Test
  public void getResultForMalformedQuery() {
    // Arrange
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, TUPLE_QUERY).build();
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL,
        backendSource.getQuery())).thenThrow(MalformedQueryException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be prepared: %s", TUPLE_QUERY));

    // Act
    backendSource.getResult();
  }

  @Test
  public void getResultForEvaluationError() {
    // Arrange
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, TUPLE_QUERY).build();
    TupleQuery query = mock(TupleQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL,
        backendSource.getQuery())).thenReturn(query);
    when(query.evaluate()).thenThrow(QueryEvaluationException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be evaluated: %s", TUPLE_QUERY));

    // Act
    backendSource.getResult();
  }

}
