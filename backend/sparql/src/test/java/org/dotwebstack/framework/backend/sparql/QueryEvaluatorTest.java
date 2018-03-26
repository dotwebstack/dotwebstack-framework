package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.query.SPARQLUpdate;
import org.eclipse.rdf4j.rio.RDFParseException;
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

  private static final String INSERT_QUERY = "INSERT DATA {"
      + "<http://dbeerpedia.org/id/brewery/0c0d7df2-a830-11e7-abc4-cec278b6b50a>"
      + " <http://www.w3.org/2000/01/rdf-schema#label> \"Maximus\" .}";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private RepositoryConnection repositoryConnection;

  @Mock
  private Model model;

  @Mock
  private IRI targetGraphIri;

  private QueryEvaluator queryEvaluator;

  @Before
  public void setUp() {
    queryEvaluator = new QueryEvaluator();
  }

  @Test
  public void evaluate_GivesGraphQueryResult_WithGraphQuery() {
    // Arrange
    GraphQuery query = mock(GraphQuery.class);
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, GRAPH_QUERY)).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    // Act
    Object result = queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY, ImmutableMap.of());

    // Assert
    assertThat(result, instanceOf(GraphQueryResult.class));
  }

  @Test
  public void evaluate_GivesTupleQueryResult_WithTupleQuery() {
    // Arrange
    TupleQuery query = mock(TupleQuery.class);
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, TUPLE_QUERY)).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    // Act
    Object result = queryEvaluator.evaluate(repositoryConnection, TUPLE_QUERY, ImmutableMap.of());

    // Assert
    assertThat(result, instanceOf(TupleQueryResult.class));
  }

  @Test
  public void add_AddModel_WithValidModel() {
    // Act
    queryEvaluator.add(repositoryConnection, model, targetGraphIri);

    // Assert
    verify(repositoryConnection, times(1)).add(model, targetGraphIri);
  }

  @Test
  public void execute_ExecuteQuery_WithValidSparqlUpdateQuery() {
    // Arrange
    SPARQLUpdate query = mock(SPARQLUpdate.class);
    when(repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, INSERT_QUERY)).thenReturn(query);

    ImmutableMap<String, Value> bindings = ImmutableMap.of("dateOfFoundation",
        DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION, "fte", DBEERPEDIA.BROUWTOREN_FTE);

    // Act
    queryEvaluator.update(repositoryConnection, INSERT_QUERY, bindings);

    // Assert
    verify(query, times(1)).execute();
  }

  @Test
  public void evaluate_SetsBindings() {
    // Arrange
    GraphQuery query = mock(GraphQuery.class);
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, GRAPH_QUERY)).thenReturn(query);
    when(query.evaluate()).thenReturn(queryResult);

    ImmutableMap<String, Value> bindings = ImmutableMap.of("dateOfFoundation",
        DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION, "fte", DBEERPEDIA.BROUWTOREN_FTE);

    // Act
    Object result = queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY, bindings);

    // Assert
    assertThat(result, instanceOf(GraphQueryResult.class));

    verify(query).setBinding("dateOfFoundation", DBEERPEDIA.BROUWTOREN_DATE_OF_FOUNDATION);
    verify(query).setBinding("fte", DBEERPEDIA.BROUWTOREN_FTE);
  }

  @Test
  public void evaluate_ThrowsException_WithUnsupportedQuery() {
    // Arrange
    BooleanQuery query = mock(BooleanQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, BOOLEAN_QUERY)).thenReturn(query);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query type '%s' not supported.", query.getClass()));

    // Act
    queryEvaluator.evaluate(repositoryConnection, BOOLEAN_QUERY, ImmutableMap.of());
  }

  @Test
  public void evaluate_ThrowsException_WithMalformedQuery() {
    // Arrange
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, TUPLE_QUERY)).thenThrow(
        MalformedQueryException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be prepared: %s", TUPLE_QUERY));

    // Act
    queryEvaluator.evaluate(repositoryConnection, TUPLE_QUERY, ImmutableMap.of());
  }

  @Test
  public void evaluate_ThrowsException_WithTupleEvalutationError() {
    // Arrange
    TupleQuery query = mock(TupleQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, TUPLE_QUERY)).thenReturn(query);
    when(query.evaluate()).thenThrow(QueryEvaluationException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be evaluated: %s", TUPLE_QUERY));

    // Act
    queryEvaluator.evaluate(repositoryConnection, TUPLE_QUERY, ImmutableMap.of());
  }

  @Test
  public void evaluate_ThrowsException_WithGraphEvalutationError() {
    // Arrange
    GraphQuery query = mock(GraphQuery.class);
    when(repositoryConnection.prepareQuery(QueryLanguage.SPARQL, GRAPH_QUERY)).thenReturn(query);
    when(query.evaluate()).thenThrow(QueryEvaluationException.class);

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be evaluated: %s", GRAPH_QUERY));

    // Act
    queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY, ImmutableMap.of());
  }

  @Test
  public void execute_ThrowsException_WithMalformedQuery() {
    // Arrange
    when(repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, INSERT_QUERY)).thenThrow(
        MalformedQueryException.class);

    ImmutableMap<String, Value> bindings = ImmutableMap.of();

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be prepared: %s", INSERT_QUERY));

    // Act
    queryEvaluator.update(repositoryConnection, INSERT_QUERY, bindings);
  }

  @Test
  public void execute_ThrowsException_WithEvalutationError() {
    // Arrange
    Update query = mock(Update.class);
    when(repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, GRAPH_QUERY)).thenReturn(query);
    doThrow(new QueryEvaluationException()).when(query).execute();
    ImmutableMap<String, Value> bindings = ImmutableMap.of();

    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format("Query could not be executed: %s", GRAPH_QUERY));

    // Act
    queryEvaluator.update(repositoryConnection, GRAPH_QUERY, bindings);
  }

  @Test
  public void add_ThrowsException_WithRdf4jError() {
    // Arrange
    doThrow(new RDFParseException("Parse error")).when(repositoryConnection).add(model,
        targetGraphIri);

    // Assert
    thrown.expect(BackendException.class);

    // Act
    queryEvaluator.add(repositoryConnection, model, targetGraphIri);
  }

}
