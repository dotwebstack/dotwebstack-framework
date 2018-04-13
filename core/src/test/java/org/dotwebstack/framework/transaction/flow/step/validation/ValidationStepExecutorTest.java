package org.dotwebstack.framework.transaction.flow.step.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.validation.RdfModelTransformer;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.class)
public class ValidationStepExecutorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ShaclValidator shaclValidator;

  @Mock
  private Resource validDataResource;

  @Mock
  private Resource invalidDataResource;

  @Mock
  private Resource shapesResource;

  private Model transactionModel;

  private ValidationStep validationStep;

  private ValidationStepExecutor validationStepExecutor;

  private Collection<Parameter> parameters = new ArrayList<>();

  private Map<String, String> parameterValues = new HashMap<>();

  @Before
  public void setUp() throws IOException {
    transactionModel = new LinkedHashModel();
    shaclValidator = mock(ShaclValidator.class);
    when(shapesResource.getInputStream()).thenReturn(new InputStreamResource(
        new ClassPathResource("/shaclvalidation/shapes.trig").getInputStream()).getInputStream());

    when(validDataResource.getInputStream()).thenReturn(
        new InputStreamResource(new ClassPathResource(
            "/shaclvalidation/validData.trig").getInputStream()).getInputStream());
    when(invalidDataResource.getInputStream()).thenReturn(
        new InputStreamResource(new ClassPathResource(
            "/shaclvalidation/invalidData.trig").getInputStream()).getInputStream());
  }

  @Test
  public void constructor_ThrowsNoErrors_WithValidObjects() {
    // Arrange
    final Model validationModel = new LinkedHashModel();
    validationStep = new ValidationStep.Builder(ELMO.VALIDATION_STEP, validationModel).conformsTo(
        ELMO.SHACL_CONCEPT_GRAPHNAME).build();
    validationStepExecutor = new ValidationStepExecutor(validationStep, transactionModel);

    // Act
    validationStepExecutor.execute(parameters, parameterValues);
  }

  @Test
  public void constructor_ThrowsNoErrors_WithValidData() throws Exception {
    // Arrange
    transactionModel = RdfModelTransformer.getModel(validDataResource.getInputStream());
    final Model validationModel = RdfModelTransformer.getModel(shapesResource.getInputStream());

    validationStep = new ValidationStep.Builder(ELMO.VALIDATION_STEP, validationModel).conformsTo(
        ELMO.SHACL_CONCEPT_GRAPHNAME).build();
    validationStepExecutor = new ValidationStepExecutor(validationStep, transactionModel);

    // Act
    validationStepExecutor.execute(parameters, parameterValues);
  }

  @Test
  public void validate_getShaclValidationReport_invalidConfiguration() throws Exception {
    //
    transactionModel = RdfModelTransformer.getModel(invalidDataResource.getInputStream());
    final Model validationModel = RdfModelTransformer.getModel(shapesResource.getInputStream());

    validationStep = new ValidationStep.Builder(ELMO.VALIDATION_STEP, validationModel).conformsTo(
        ELMO.SHACL_CONCEPT_GRAPHNAME).build();
    validationStepExecutor = new ValidationStepExecutor(validationStep, transactionModel);

    // Act
    thrown.expect(ShaclValidationException.class);
    validationStepExecutor.execute(parameters, parameterValues);

    // Act
    final ValidationReport report =
        shaclValidator.validate(RdfModelTransformer.getModel(invalidDataResource.getInputStream()),
            RdfModelTransformer.getModel(shapesResource.getInputStream()));

    // Assert
    assertThat(report.isValid(), equalTo(false));
    assertThat(report.getErrors().size(), equalTo(1));
    final String errorKey = report.getErrors().keySet().iterator().next();
    assertThat(report.getErrors().get(errorKey).getReport(),
        equalTo("Invalid configuration at path [http://example.org#lid] on node "
            + "[http://example.org#HuwelijkMarcoNanda] with error message [More than 2 values]"));
  }

}
