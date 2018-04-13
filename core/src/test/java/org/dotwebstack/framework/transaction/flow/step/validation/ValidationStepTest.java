package org.dotwebstack.framework.transaction.flow.step.validation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationStepTest {

  @Mock
  private Resource identifier;

  @Mock
  private RepositoryConnection repositoryConnection;

  private ValidationStep validationStep;

  private String label;

  private IRI conformsTo;

  private Model validationModel;

  @Before
  public void setUp() {
    validationModel = new LinkedHashModel();
    label = DBEERPEDIA.BREWERIES_LABEL.toString();
    conformsTo = DBEERPEDIA.APPLIES_TO;
  }

  @Test
  public void build_CreateValidationStep_WithValidData() {
    // Arrange
    validationModel = new LinkedHashModel();

    // Act
    validationStep =
        new ValidationStep.Builder(identifier, validationModel).label(label).conformsTo(
            conformsTo).build();

    // Assert
    assertThat(validationStep.getIdentifier(), equalTo(identifier));
    assertThat(validationStep.getConformsTo(), equalTo(conformsTo));
    assertThat(validationStep.getLabel(), equalTo(label));
    assertThat(validationStep.getValidationModel(), equalTo(validationModel));
  }

  @Test
  public void createStepExecutor_GetValidationStepExecutor_WithValidData() {
    // Arrange
    validationStep =
        new ValidationStep.Builder(identifier, validationModel).conformsTo(conformsTo).build();
    when(repositoryConnection.getStatements(null, null, null)).thenReturn(
        mock(RepositoryResult.class));

    // Act
    StepExecutor stepExecutor = validationStep.createStepExecutor(repositoryConnection);

    // Assert
    assertThat(stepExecutor, instanceOf(ValidationStepExecutor.class));
  }

}
