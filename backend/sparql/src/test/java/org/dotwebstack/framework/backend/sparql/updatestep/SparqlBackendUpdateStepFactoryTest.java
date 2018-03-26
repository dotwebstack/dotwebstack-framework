package org.dotwebstack.framework.backend.sparql.updatestep;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.update.UpdateStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendUpdateStepFactoryTest {

  private SparqlBackendUpdateStepFactory sparqlBackendUpdateStepFactory;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private UpdateStep updateStep;

  @Mock
  private SparqlBackend sparqlBackend;

  @Test
  public void create() {
    // Arrange
    sparqlBackendUpdateStepFactory = new SparqlBackendUpdateStepFactory(queryEvaluator);

    // Act
    UpdateStepExecutor updateStepExecutor =
        sparqlBackendUpdateStepFactory.create(updateStep, sparqlBackend);

    // Assert
    assertThat(updateStepExecutor, instanceOf(UpdateStepExecutor.class));
  }

}
