package org.dotwebstack.framework.backend.sparql.updatestep;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.update.UpdateStep;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStepExecutorTest {

  @Mock
  private UpdateStepExecutor updateStepExecutor;

  @Mock
  private UpdateStep updateStep;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private SparqlBackend sparqlBackend;

  @Mock
  private RepositoryConnection repositoryConnection;

  private Collection<Parameter> parameters = new ArrayList<>();

  private Map<String, String> parameterValues =  new HashMap<>();

  @Test
  public void execute_IsExecuted_WithValidData() {
    // Arrange
    String query = "";
    updateStepExecutor = new UpdateStepExecutor(updateStep, queryEvaluator, sparqlBackend);
    when(updateStep.getQuery()).thenReturn(query);
    when(sparqlBackend.getConnection()).thenReturn(repositoryConnection);
    ImmutableMap<String, Value> bindings = ImmutableMap.of();

    // Act
    updateStepExecutor.execute(parameters, parameterValues);

    // Assert
    verify(queryEvaluator, times(1)).update(repositoryConnection, query, bindings);
  }

}
