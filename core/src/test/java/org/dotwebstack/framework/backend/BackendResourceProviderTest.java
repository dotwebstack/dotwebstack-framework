package org.dotwebstack.framework.backend;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
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
public class BackendResourceProviderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private BackendFactory backendFactory;

  @Mock
  private Backend backend;

  private BackendResourceProvider backendResourceProvider;

  private List<BackendFactory> backendFactories;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    backendFactories = ImmutableList.of(backendFactory);
    backendResourceProvider =
        new BackendResourceProvider(configurationBackend, backendFactories, applicationProperties);

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
    new BackendResourceProvider(null, backendFactories, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingBackendFactoriesProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new BackendResourceProvider(configurationBackend, null, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new BackendResourceProvider(configurationBackend, backendFactories, null);
  }

  @Test
  public void get_ThrowsException_BackendNotFound() {
    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.format("Resource <%s> not found.", DBEERPEDIA.BACKEND));

    // Act
    backendResourceProvider.get(DBEERPEDIA.BACKEND);
  }

  @Test
  public void loadResources_GetResources_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BACKEND, RDF.TYPE, ELMO.SPARQL_BACKEND),
            valueFactory.createStatement(DBEERPEDIA.BACKEND, ELMO.ENDPOINT_PROP,
                DBEERPEDIA.ENDPOINT))));
    when(backendFactory.create(any(Model.class), eq(DBEERPEDIA.BACKEND))).thenReturn(backend);
    when(backendFactory.supports(any(IRI.class))).thenReturn(true);

    // Act
    backendResourceProvider.loadResources();

    // Assert
    assertThat(backendResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(backendResourceProvider.get(DBEERPEDIA.BACKEND), equalTo(backend));
  }

  @Test
  public void loadResources_ThrowsException_NoBackendFactoryFound() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BACKEND, RDF.TYPE, ELMO.SPARQL_BACKEND),
            valueFactory.createStatement(DBEERPEDIA.BACKEND, ELMO.ENDPOINT_PROP,
                DBEERPEDIA.ENDPOINT))));
    when(backendFactory.supports(ELMO.SPARQL_BACKEND)).thenReturn(false);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No backend factory available for type <%s>.", ELMO.SPARQL_BACKEND));

    // Act
    backendResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_TypeStatementMissing() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.BACKEND, ELMO.ENDPOINT_PROP,
            DBEERPEDIA.ENDPOINT))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for backend <%s>.",
        RDF.TYPE, DBEERPEDIA.BACKEND));

    // Act
    backendResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_RepositoryConnectionError() {
    // Arrange
    when(configurationRepository.getConnection()).thenThrow(RepositoryException.class);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while getting repository connection.");

    // Act
    backendResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_QueryEvaluationError() {
    // Arrange
    when(graphQuery.evaluate()).thenThrow(QueryEvaluationException.class);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while evaluating SPARQL query.");

    // Act
    backendResourceProvider.loadResources();
  }

}
