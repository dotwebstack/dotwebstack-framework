package org.dotwebstack.framework.transaction.flow;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SequentialFlowExecutorTest {

  @Mock
  private SequentialFlow sequentialFlow;

  @Mock
  private Step step;

  @Mock
  private StepExecutor stepExecutor;

  @Mock
  private RepositoryConnection repositoryConnection;

  private SequentialFlowExecutor sequentialFlowExecutor;

  private Collection<Parameter> parameters = new ArrayList<>();

  private Map<String, String> parameterValues =  new HashMap<>();

  @Before
  public void setUp() {
    sequentialFlowExecutor = new SequentialFlowExecutor(sequentialFlow, repositoryConnection);
  }

  @Test
  public void executeFlow_execute1Step_WithValidData() {
    // Arrange
    List<Step> listSteps = new ArrayList<>();
    listSteps.add(step);
    when(sequentialFlow.getSteps()).thenReturn(listSteps);
    when(step.createStepExecutor(repositoryConnection)).thenReturn(stepExecutor);

    // Act
    sequentialFlowExecutor.execute(parameters, parameterValues);

    // Assert
    verify(stepExecutor, times(1)).execute(parameters, parameterValues);
  }

}
