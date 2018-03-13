package org.dotwebstack.framework.transaction.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.step.StepResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
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
public class SequentialFlowResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private StepResourceProvider stepResourceProvider;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  private PersistenceStep persistenceStep;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private SequentialFlowResourceProvider sequentialFlowResourceProvider;

  @Before
  public void setUp() {
    // Arrange
    persistenceStep = new PersistenceStep.Builder(DBEERPEDIA.PERSISTENCE_STEP,
        backendResourceProvider).build();
    sequentialFlowResourceProvider = new SequentialFlowResourceProvider(configurationBackend,
        applicationProperties, stepResourceProvider);

    when(stepResourceProvider.get(any())).thenReturn(persistenceStep);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void loadResources_LoadSequentialFlow_WithValidData() {
    // Arrange
    List<IRI> steps = Arrays.asList((new IRI[] {DBEERPEDIA.PERSISTENCE_STEP}));
    Resource headOfList = valueFactory.createBNode();
    Model flowModel = RDFCollections.asRDF(steps, headOfList, new LinkedHashModel());
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.copyOf(flowModel)));

    // Act
    sequentialFlowResourceProvider.loadResources();

    // Assert
    assertThat(sequentialFlowResourceProvider.getAll().entrySet(), hasSize(1));
    SequentialFlow sequentialFlow =
        sequentialFlowResourceProvider.getAll().entrySet().iterator().next().getValue();
    assertThat(sequentialFlow, notNullValue());
    assertThat(sequentialFlow.getSteps(), notNullValue());
  }

  @Test
  public void loadResources_ThrowException_WithEmptyList() {
    // Arrange
    List<IRI> steps = new ArrayList<>();
    Resource headOfList = valueFactory.createBNode();
    Model flowModel = RDFCollections.asRDF(steps, headOfList, new LinkedHashModel());
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.copyOf(flowModel)));

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    sequentialFlowResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowException_WhenStepDoesNotExist() {
    // Arrange
    persistenceStep = null;
    when(stepResourceProvider.get(any())).thenReturn(persistenceStep);
    List<IRI> steps = Arrays.asList((new IRI[] {DBEERPEDIA.PERSISTENCE_STEP}));
    Resource headOfList = valueFactory.createBNode();
    Model flowModel = RDFCollections.asRDF(steps, headOfList, new LinkedHashModel());
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.copyOf(flowModel)));

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    sequentialFlowResourceProvider.loadResources();
  }

}
