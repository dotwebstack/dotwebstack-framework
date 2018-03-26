package org.dotwebstack.framework.transaction;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
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
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.FlowFactory;
import org.dotwebstack.framework.transaction.flow.SequentialFlow;
import org.dotwebstack.framework.transaction.flow.SequentialFlowFactory;
import org.dotwebstack.framework.transaction.flow.SequentialFlowResourceProvider;
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
public class TransactionResourceProviderTest {

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
  private SequentialFlow sequentialFlow;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private SequentialFlowResourceProvider sequentialFlowResourceProvider;

  @Mock
  private ParameterDefinitionResourceProvider parameterDefinitionResourceProvider;

  private List<FlowFactory> flowFactories;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private TransactionResourceProvider transactionResourceProvider;

  @Before
  public void setUp() {
    flowFactories = new ArrayList<>();
    flowFactories.add(new SequentialFlowFactory(sequentialFlowResourceProvider));

    transactionResourceProvider =
        new TransactionResourceProvider(configurationBackend, applicationProperties, flowFactories,
            parameterDefinitionResourceProvider);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(sequentialFlowResourceProvider.get(any())).thenReturn(sequentialFlow);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TransactionResourceProvider(null, applicationProperties, flowFactories,
        parameterDefinitionResourceProvider);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TransactionResourceProvider(configurationBackend, null, flowFactories,
        parameterDefinitionResourceProvider);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationFlowFactories() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TransactionResourceProvider(configurationBackend, applicationProperties, null,
        parameterDefinitionResourceProvider);
  }

  @Test
  public void loadResources_ThrowsException_WithNoMatchingFlowFactories() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.TRANSACTION, RDF.TYPE, ELMO.TRANSACTION),
            valueFactory.createStatement(DBEERPEDIA.TRANSACTION, ELMO.UNKNOWN_FLOW_PROP,
                DBEERPEDIA.PERSISTENCE_STEP))));

    // Act
    transactionResourceProvider.loadResources();
  }

  @Test
  public void loadResources_LoadStage_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.TRANSACTION, RDF.TYPE, ELMO.TRANSACTION),
            valueFactory.createStatement(DBEERPEDIA.TRANSACTION, ELMO.SEQUENTIAL_FLOW_PROP,
                DBEERPEDIA.PERSISTENCE_STEP))));

    // Act
    transactionResourceProvider.loadResources();

    // Assert
    assertThat(transactionResourceProvider.getAll().entrySet(), hasSize(1));
    Transaction transaction = transactionResourceProvider.get(DBEERPEDIA.TRANSACTION);
    assertThat(transaction, notNullValue());
    assertThat(transaction.getFlow(), notNullValue());
  }
}
