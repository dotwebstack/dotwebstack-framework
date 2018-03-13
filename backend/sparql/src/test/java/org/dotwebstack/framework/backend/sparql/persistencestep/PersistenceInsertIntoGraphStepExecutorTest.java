package org.dotwebstack.framework.backend.sparql.persistencestep;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceInsertIntoGraphStepExecutorTest {

  private PersistenceInsertIntoGraphStepExecutor persistenceInsertIntoGraphStepExecutor;

  @Mock
  private PersistenceStep persistenceStep;

  @Mock
  private Model transactionModel;

  @Mock
  private SparqlBackend sparqlBackend;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Test
  public void execute_AddModelIntoGraph_WithValidData() {
    // Arrange
    persistenceInsertIntoGraphStepExecutor = new PersistenceInsertIntoGraphStepExecutor(
        persistenceStep, transactionModel, sparqlBackend, queryEvaluator);

    // Act
    persistenceInsertIntoGraphStepExecutor.execute();

    // Assert
    verify(queryEvaluator, times(1)).add(any(), any(), any());
  }

}
