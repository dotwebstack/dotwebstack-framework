package org.dotwebstack.framework.backend;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
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
public class BackendLoaderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private BackendFactory backendFactory;

  @Mock
  private Backend backend;

  private BackendLoader backendLoader;

  private List<BackendFactory> backendFactories;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    backendFactories = ImmutableList.of(backendFactory);
    backendLoader = new BackendLoader(configurationBackend, backendFactories);
    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);
  }

  @Test
  public void backendNotFound() {
    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.format("Backend <%s> not found.", DBEERPEDIA.BACKEND));

    // Act
    backendLoader.getBackend(DBEERPEDIA.BACKEND);
  }

  @Test
  public void loadBackend() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BACKEND, RDF.TYPE, ELMO.SPARQL_BACKEND),
            valueFactory.createStatement(DBEERPEDIA.BACKEND, ELMO.ENDPOINT, DBEERPEDIA.ENDPOINT))));
    when(backendFactory.create(any(Model.class), eq(DBEERPEDIA.BACKEND))).thenReturn(backend);
    when(backendFactory.supports(any(IRI.class))).thenReturn(true);
    when(backend.getIdentifier()).thenReturn(DBEERPEDIA.BREWERIES);

    // Act
    backendLoader.load();

    // Assert
    assertThat(backendLoader.getNumberOfBackends(), equalTo(1));
    assertThat(backendLoader.getBackend(DBEERPEDIA.BACKEND), equalTo(backend));
  }

  @Test
  public void noBackendFactoryFound() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BACKEND, RDF.TYPE, ELMO.SPARQL_BACKEND),
            valueFactory.createStatement(DBEERPEDIA.BACKEND, ELMO.ENDPOINT, DBEERPEDIA.ENDPOINT))));
    when(backendFactory.supports(ELMO.SPARQL_BACKEND)).thenReturn(false);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No backend factories available for type <%s>.", ELMO.SPARQL_BACKEND));

    // Act
    backendLoader.load();
  }

  @Test
  public void typeStatementMissing() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(
        new IteratingGraphQueryResult(ImmutableMap.of(), ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BACKEND, ELMO.ENDPOINT, DBEERPEDIA.ENDPOINT))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for backend <%s>.",
        RDF.TYPE, DBEERPEDIA.BACKEND));

    // Act
    backendLoader.load();
  }

}
