package org.dotwebstack.framework.backend.sparql.validationstep;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.transaction.flow.step.validation.ValidationStep;
import org.eclipse.rdf4j.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendValidationStepFactoryTest {

  private SparqlBackendValidationStepFactory sparqlBackendValidationStepFactory;

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private ValidationStep validationStep;

  @Mock
  private FileConfigurationBackend fileConfigurationBackend;

  @Mock
  private Model transactionModel;

  @Test
  public void create() {
    // Arrange
    sparqlBackendValidationStepFactory = new SparqlBackendValidationStepFactory(queryEvaluator);

    // Act
    ValidationStepExecutor validationStepExecutor = sparqlBackendValidationStepFactory.create(
        validationStep, transactionModel, fileConfigurationBackend);

    // Assert
    assertThat(validationStepExecutor, instanceOf(ValidationStepExecutor.class));
  }

}
