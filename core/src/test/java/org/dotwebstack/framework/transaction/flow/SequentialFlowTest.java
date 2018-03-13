package org.dotwebstack.framework.transaction.flow;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SequentialFlowTest {

  @Mock
  private Resource resource;

  @Mock
  private List<Step> stepList;

  @Mock
  private RepositoryConnection repositoryConnection;

  @Before
  public void setUp() {
  }

  @Test
  public void build_CreatesSequentialFlow_WithValidData() {
    // Act
    SequentialFlow sequentialFlow = new SequentialFlow.Builder(resource, stepList).build();

    // Assert
    assertThat(sequentialFlow.getIdentifier(), equalTo(resource));
    assertThat(sequentialFlow.getSteps(), equalTo(stepList));
    assertThat(sequentialFlow.getExecutor(repositoryConnection),
        IsInstanceOf.instanceOf(SequentialFlowExecutor.class));
  }

}
