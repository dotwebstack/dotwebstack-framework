package org.dotwebstack.framework.backend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.dotwebstack.framework.Registry;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class BackendLoaderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Registry registry;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private BackendFactory backendFactory;

  @Mock
  private Backend backend;

  private BackendLoader backendLoader;

  private SailRepository configurationRepository;

  private List<BackendFactory> backendFactories;

  @Before
  public void setUp() throws RDF4JException, IOException {
    ClassPathResource elmoFile = new ClassPathResource("model/elmo.ttl");
    ClassPathResource configFile = new ClassPathResource("model/dbpeerpedia.ttl");
    backendFactories = ImmutableList.of(backendFactory);
    backendLoader = new BackendLoader(registry, configurationBackend, backendFactories);
    configurationRepository = new SailRepository(new MemoryStore());
    configurationRepository.initialize();
    configurationRepository.getConnection().add(elmoFile.getInputStream(), "#", RDFFormat.TURTLE);
    configurationRepository.getConnection().add(configFile.getInputStream(), "#", RDFFormat.TURTLE);
    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
  }

  @Test
  public void loadBackend() throws RDF4JException, IOException {
    // Arrange
    when(backendFactory.create(any(Model.class), eq(DBEERPEDIA.BACKEND))).thenReturn(backend);
    when(backendFactory.supports(any(IRI.class))).thenReturn(true);
    when(backend.getIdentifier()).thenReturn(DBEERPEDIA.BREWERIES);

    // Act
    backendLoader.load();

    // Assert
    verify(registry, times(1)).registerBackend(backend);
    verifyNoMoreInteractions(registry);
  }

  @Test
  public void noBackendFactoryFound() throws RDF4JException, IOException {
    // Arrange
    when(backendFactory.supports(ELMO.SPARQL_BACKEND)).thenReturn(false);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No backend factories available for type <%s>.", ELMO.SPARQL_BACKEND));

    // Act
    backendLoader.load();
  }

}
