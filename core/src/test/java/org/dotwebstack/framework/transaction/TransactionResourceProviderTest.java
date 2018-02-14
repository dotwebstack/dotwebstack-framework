package org.dotwebstack.framework.transaction;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.FlowFactory;
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
  private GraphQuery graphQuery;

  private List<FlowFactory> flowFactories;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private TransactionResourceProvider transactionResourceProvider;

  @Before
  public void setUp() {
    transactionResourceProvider =
        new TransactionResourceProvider(configurationBackend, applicationProperties, flowFactories);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TransactionResourceProvider(null, applicationProperties, flowFactories);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TransactionResourceProvider(configurationBackend, null, flowFactories);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationFlowFactories() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TransactionResourceProvider(configurationBackend, applicationProperties, null);
  }

  @Test
  public void loadResources_LoadStage_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.TRANSACTION, RDF.TYPE, ELMO.TRANSACTION),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.SITE_PROP, DBEERPEDIA.SITE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.BASE_PATH, DBEERPEDIA.BASE_PATH),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.LAYOUT_PROP, DBEERPEDIA.LAYOUT))));

    // Act
    transactionResourceProvider.loadResources();

    // Assert
    assertThat(transactionResourceProvider.getAll().entrySet(), hasSize(1));
    Transaction transaction = transactionResourceProvider.get(DBEERPEDIA.TRANSACTION);
    assertThat(transaction, notNullValue());
    assertThat(transaction.getFlow(), notNullValue());
    // assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
  }
}
