package org.dotwebstack.framework.transaction.flow.step.validation;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationStepFactoryTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend backend;

  @Mock
  private SailRepository repository;

  @Mock
  private SailRepositoryConnection repositoryConnection;

  private ValidationStepFactory validationStepFactory;

  private ValueFactory valueFactory;

  private ValidationStep validationStep;

  @Before
  public void setup() {
    // Arrange
    when(backend.getRepository()).thenReturn(repository);
    when(repository.getConnection()).thenReturn(repositoryConnection);
    validationStepFactory = new ValidationStepFactory(backend);
    valueFactory = SimpleValueFactory.getInstance();
  }

  @Test
  public void supports_ReturnTrue_WhenSupported() {
    // Act/Assert
    assertTrue(validationStepFactory.supports(ELMO.VALIDATION_STEP));
  }

  @Test
  public void supports_ReturnTrue_WhenNotSupported() {
    // Act/Assert
    assertFalse(validationStepFactory.supports(ELMO.UPDATE_STEP));
  }

  @Test
  public void create_CreateValidationStep_WithValidData() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(
        valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, RDF.TYPE, ELMO.VALIDATION_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, ELMO.CONFORMS_TO_PROP,
        DBEERPEDIA.APPLIES_TO));

    // Act
    when(backend.getRepository().getConnection().getStatements(null, null, null,
        DBEERPEDIA.APPLIES_TO)).thenReturn(mock(RepositoryResult.class));
    validationStep = validationStepFactory.create(stepModel, DBEERPEDIA.VALIDATION_STEP);

    // Assert
    assertThat(validationStep.getIdentifier(), equalTo(DBEERPEDIA.VALIDATION_STEP));
    assertThat(validationStep.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(validationStep.getConformsTo(), equalTo(DBEERPEDIA.APPLIES_TO));
  }

  @Test
  public void create_ThrowConfigurationException_ByMissingConformsTo() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(
        valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, RDF.TYPE, ELMO.VALIDATION_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL));

    // Act / Assert
    thrown.expect(ConfigurationException.class);
    validationStep = validationStepFactory.create(stepModel, DBEERPEDIA.VALIDATION_STEP);
  }

  @Test
  public void create_ThrowBackendException_ByRdf4JException() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(
        valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, RDF.TYPE, ELMO.VALIDATION_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, RDFS.LABEL,
        DBEERPEDIA.BREWERIES_LABEL));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.VALIDATION_STEP, ELMO.CONFORMS_TO_PROP,
        DBEERPEDIA.APPLIES_TO));

    // Act / Assert
    thrown.expect(BackendException.class);
    when(backend.getRepository().getConnection().getStatements(null, null, null,
        DBEERPEDIA.APPLIES_TO)).thenThrow(mock(RDF4JException.class));
    validationStep = validationStepFactory.create(stepModel, DBEERPEDIA.VALIDATION_STEP);
  }

}
