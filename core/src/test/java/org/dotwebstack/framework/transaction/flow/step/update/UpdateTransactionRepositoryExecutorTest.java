package org.dotwebstack.framework.transaction.flow.step.update;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTransactionRepositoryExecutorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private UpdateTransactionRepositoryExecutor updateTransactionRepositoryExecutor;

  @Mock
  private UpdateStep updateStep;

  private Repository repository;

  private RepositoryConnection repositoryConnection;

  private Collection<Parameter> parameters = new ArrayList<>();

  private Map<String, String> parameterValues =  new HashMap<>();

  @Before
  public void setUp() {
    repository = new SailRepository(new MemoryStore());
    repository.initialize();
    repositoryConnection = repository.getConnection();

    updateTransactionRepositoryExecutor =
        new UpdateTransactionRepositoryExecutor(updateStep, repositoryConnection);
  }

  @Test
  public void execute_ExecuteUpdate_WithValidSparqlUpdate() {
    // Arrange
    when(updateStep.getQuery()).thenReturn("INSERT DATA " + "{"
        + "<http://dbeerpedia.org/id/brewery/0c0d7df2-a830-11e7-abc4-cec278b6b50a>"
        + " <http://www.w3.org/2000/01/rdf-schema#label> \"Maximus\" ." + "}");

    // Act
    updateTransactionRepositoryExecutor.execute(parameters, parameterValues);

    // Assert
    GraphQueryResult graphQueryResult =
        ((GraphQuery) repositoryConnection.prepareQuery(QueryLanguage.SPARQL,
            "construct { ?s ?p ?o } where { ?s ?p ?o }")).evaluate();
    Model model = QueryResults.asModel(graphQueryResult);
    assertTrue(model.contains(DBEERPEDIA.MAXIMUS, RDFS.LABEL, DBEERPEDIA.MAXIMUS_NAME));
  }

  @Test
  public void execute_ExpectBackendException_WithInvalidSparqlUpdate() {
    // Arrange
    when(updateStep.getQuery()).thenReturn("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");

    // Assert
    thrown.expect(Exception.class);

    // Act
    updateTransactionRepositoryExecutor.execute(parameters, parameterValues);
  }

  @After
  public void tearDown() {
    repositoryConnection.close();
    repository.shutDown();
  }

}
