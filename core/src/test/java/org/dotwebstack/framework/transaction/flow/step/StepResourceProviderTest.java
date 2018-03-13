package org.dotwebstack.framework.transaction.flow.step;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
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
public class StepResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private PersistenceStep persistenceStep;

  @Mock
  private StepFactory stepFactory;

  private List<StepFactory> stepFactoryList = new ArrayList<>();

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  StepResourceProvider stepResourceProvider;

  @Before
  public void setUp() {
    when(stepFactory.supports(any())).thenReturn(TRUE);
    when(stepFactory.create(any(), any())).thenReturn(persistenceStep);

    stepFactoryList.add(stepFactory);
    stepResourceProvider = new StepResourceProvider(configurationBackend, applicationProperties,
        stepFactoryList);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void loadResources_LoadStep_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.PERSISTENCE_STEP, RDF.TYPE,
            ELMO.PERSISTENCE_STEP))));

    // Act
    stepResourceProvider.loadResources();;

    // Assert
    assertThat(stepResourceProvider.get(DBEERPEDIA.PERSISTENCE_STEP), equalTo(persistenceStep));
  }

  @Test
  public void loadResources_ThrowConfigurationException_WhenStepFactoryIsNotSupported() {
    // Arrange
    when(stepFactory.supports(any())).thenReturn(FALSE);
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.PERSISTENCE_STEP, RDF.TYPE,
            ELMO.PERSISTENCE_STEP))));

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    stepResourceProvider.loadResources();
  }

}
