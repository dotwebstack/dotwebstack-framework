package org.dotwebstack.framework.transaction.flow.step.assertion;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
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
public class AssertionTransactionRepositoryExecutorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private AssertionTransactionRepositoryExecutor assertionTransactionRepositoryExecutor;

  @Mock
  private AssertionStep assertionStep;

  private Repository repository;

  private RepositoryConnection repositoryConnection;

  private Collection<Parameter> parameters = new ArrayList<>();

  private Map<String, String> parameterValues =  new HashMap<>();

  @Before
  public void setUp() {
    repository = new SailRepository(new MemoryStore());
    repository.initialize();
    repositoryConnection = repository.getConnection();

    assertionTransactionRepositoryExecutor =
        new AssertionTransactionRepositoryExecutor(assertionStep, repositoryConnection);
  }

  @Test
  public void execute_ExecuteAssertion_WithValidSparqlAssertion() {
    // Arrange
    when(assertionStep.isAssertionNot()).thenReturn(false);
    when(assertionStep.getAssertionQuery()).thenReturn("ASK WHERE { ?s ?p ?o }");

    // Assert
    thrown.expect(StepFailureException.class);

    // Act
    assertionTransactionRepositoryExecutor.execute(parameters, parameterValues);
  }

  @Test
  public void execute_ExecuteAssertionNot_WithValidSparqlAssertion() {
    // Arrange
    when(assertionStep.isAssertionNot()).thenReturn(true);
    when(assertionStep.getAssertionQuery()).thenReturn("ASK WHERE { ?s ?p ?o }");

    // Act
    assertionTransactionRepositoryExecutor.execute(parameters, parameterValues);
  }

  @Test
  public void execute_ExpectBackendException_WithInvalidSparqlAssertion() {
    // Arrange
    when(assertionStep.getAssertionQuery()).thenReturn("SELECT ?s ?p ?o WHERE { ?s ?p ?o }");

    // Assert
    thrown.expect(Exception.class);

    // Act
    assertionTransactionRepositoryExecutor.execute(parameters, parameterValues);
  }

  @After
  public void tearDown() {
    repositoryConnection.close();
    repository.shutDown();
  }

}
