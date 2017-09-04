package org.dotwebstack.framework.backend.sparql;

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
public class QueryEvaluatorTest {

  private static final String GRAPH_QUERY = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o}";

  private static final String TUPLE_QUERY = "SELECT { ?s ?p ?o } WHERE { ?s ?p ?o}";

  private static final String BOOLEAN_QUERY = "ASK { ?s ?p ?o}";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private RepositoryConnection repositoryConnection;

  private QueryEvaluator queryEvaluator;

  @Before
  public void setUp() {
    queryEvaluator = new QueryEvaluator();
  }

  @Test
  public void evaluateGraphQuery() {
    // Arrange
    GraphQuery query = mock(GraphQuery.class);
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, GRAPH_QUERY)).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    // Act
    Object result = queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY);

    // Assert
    assertThat(result, instanceOf(GraphQueryResult.class));
  }

  @Test
  public void evaluateTupleQuery() {
    // Arrange
    TupleQuery query = mock(TupleQuery.class);
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, TUPLE_QUERY)).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    // Act
    Object result = queryEvaluator.evaluate(repositoryConnection, TUPLE_QUERY);

    // Assert
    assertThat(result, instanceOf(TupleQueryResult.class));
  }

  @Test
  public void evaluateUnsupportedQueryType() {
    // Arrange
    BooleanQuery query = mock(BooleanQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, BOOLEAN_QUERY)).thenReturn(query);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query type '%s' not supported.", query.getClass()));

    // Act
    queryEvaluator.evaluate(repositoryConnection, BOOLEAN_QUERY);
  }

  @Test
  public void evaluateMalformedQuery() {
    // Arrange
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, TUPLE_QUERY)).thenThrow(
        MalformedQueryException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be prepared: %s", TUPLE_QUERY));

    // Act
    queryEvaluator.evaluate(repositoryConnection, TUPLE_QUERY);
  }

  @Test
  public void evaluateError() {
    // Arrange
    TupleQuery query = mock(TupleQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, TUPLE_QUERY)).thenReturn(query);
    when(query.evaluate()).thenThrow(QueryEvaluationException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be evaluated: %s", TUPLE_QUERY));

    // Act
    queryEvaluator.evaluate(repositoryConnection, TUPLE_QUERY);
  }

}
