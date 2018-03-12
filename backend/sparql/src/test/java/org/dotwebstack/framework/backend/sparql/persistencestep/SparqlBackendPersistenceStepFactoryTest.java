package org.dotwebstack.framework.backend.sparql.persistencestep;

import static org.hamcrest.MatcherAssert.assertThat;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendPersistenceStepFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Resource identifier;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private IRI targetGraph;

  @Mock
  private Backend backend;

  @Mock
  private SparqlBackend sparqlBackend;

  @Mock
  private Model transactionModel;

  @Mock
  private QueryEvaluator queryEvaluator;

  private SparqlBackendPersistenceStepFactory sparqlBackendPersistenceStepFactory;

  private PersistenceStep persistenceStep;

  @Before
  public void setup() {
    sparqlBackendPersistenceStepFactory = new SparqlBackendPersistenceStepFactory(queryEvaluator);
  }

  @Test
  public void create_GetPersistenceStepExecutor_WhenSupported() {
    // Arrange
    persistenceStep = new PersistenceStep.Builder(identifier, backendResourceProvider)
        .persistenceStrategy(ELMO.PERSISTENCE_STRATEGY_INSERT_INTO_GRAPH).backend(backend)
        .targetGraph(targetGraph).build();

    // Act
    PersistenceInsertIntoGraphStepExecutor persistenceInsertIntoGraphStepExecutor =
        sparqlBackendPersistenceStepFactory.create(persistenceStep, transactionModel,
            sparqlBackend);

    // Assert
    assertThat(persistenceInsertIntoGraphStepExecutor,
        IsInstanceOf.instanceOf(PersistenceInsertIntoGraphStepExecutor.class));
  }

  @Test
  public void create_ThrowException_WhenStrategyIsNotSupported() {
    // Arrange
    persistenceStep = new PersistenceStep.Builder(identifier, backendResourceProvider)
        .persistenceStrategy(ELMO.PERSISTENCE_STRATEGY_UNKNOWN).backend(backend)
        .targetGraph(targetGraph).build();

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    sparqlBackendPersistenceStepFactory.create(persistenceStep, transactionModel, sparqlBackend);
  }

}
