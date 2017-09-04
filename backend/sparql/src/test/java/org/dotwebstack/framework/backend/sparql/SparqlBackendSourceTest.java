package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendSourceTest {

  private static final String GRAPH_QUERY = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o}";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private SparqlBackend backend;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Test
  public void builder() {
    // Act
    SparqlBackendSource backendSource =
        new SparqlBackendSource.Builder(backend, GRAPH_QUERY, queryEvaluator).build();

    // Assert
    assertThat(backendSource.getBackend(), equalTo(backend));
    assertThat(backendSource.getQuery(), equalTo(GRAPH_QUERY));
  }

  @Test
  public void requiredBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(null, GRAPH_QUERY, queryEvaluator).build();
  }

  @Test
  public void requiredQuery() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(backend, null, queryEvaluator).build();
  }

  @Test
  public void requiredQueryEvaluator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendSource.Builder(backend, GRAPH_QUERY, null).build();
  }

  @Test
  public void getResult() {
    // Arrange
    Object expectedResult = new Object();
    when(backend.getConnection()).thenReturn(repositoryConnection);
    when(queryEvaluator.evaluate(repositoryConnection, GRAPH_QUERY)).thenReturn(expectedResult);
    SparqlBackendSource source =
        new SparqlBackendSource.Builder(backend, GRAPH_QUERY, queryEvaluator).build();

    // Act
    Object result = source.getResult();

    // Assert
    assertThat(result, equalTo(expectedResult));
  }

}
