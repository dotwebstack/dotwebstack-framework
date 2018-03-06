package org.dotwebstack.framework.transaction.flow.step.persistence;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceStepFactoryTest {

  private PersistenceStep persistenceStep;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private Backend backend;

  private PersistenceStepFactory persistenceStepFactory;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setup() {
    // Arrange
    persistenceStepFactory = new PersistenceStepFactory(backendResourceProvider);
    when(backendResourceProvider.get(any())).thenReturn(backend);
  }

  @Test
  public void supports_ReturnTrue_WhenSupported() {
    // Act/Assert
    assertTrue(persistenceStepFactory.supports(ELMO.PERSISTENCE_STEP));
  }

  @Test
  public void supports_ReturnTrue_WhenNotSupported() {
    // Act/Assert
    assertFalse(persistenceStepFactory.supports(ELMO.VALIDATION_STEP));
  }

  @Test
  public void create_CreatePersistenceStep_WithValidData() {
    // Arrange
    Model stepModel = new LinkedHashModel();
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.PERSISTENCE_STEP, RDF.TYPE,
        ELMO.PERSISTENCE_STEP));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.PERSISTENCE_STEP,
        ELMO.PERSISTENCE_STRATEGY_PROP, ELMO.PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.PERSISTENCE_STEP, ELMO.BACKEND_PROP,
        DBEERPEDIA.BACKEND));
    stepModel.add(valueFactory.createStatement(DBEERPEDIA.PERSISTENCE_STEP, ELMO.TARGET_GRAPH_PROP,
        DBEERPEDIA.SYSTEM_GRAPH_IRI));

    // Act
    persistenceStep = persistenceStepFactory.create(stepModel, DBEERPEDIA.PERSISTENCE_STEP);

    // Assert
    assertThat(persistenceStep.getIdentifier(), equalTo(DBEERPEDIA.PERSISTENCE_STEP));
    assertThat(persistenceStep.getPersistenceStrategy(),
        equalTo(ELMO.PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH));
    assertThat(persistenceStep.getBackend(), equalTo(backend));
    assertThat(persistenceStep.getTargetGraph(), equalTo(DBEERPEDIA.SYSTEM_GRAPH_IRI));
  }

}
