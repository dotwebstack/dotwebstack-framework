package org.dotwebstack.framework.rml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.repository.RepositoryException;
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
public class RmlMappingResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  private SailRepository sailRepository;

  private RmlMappingResourceProvider rmlMappingResourceProvider;

  @Before
  public void setUp() {
    // Arrange
    rmlMappingResourceProvider = new RmlMappingResourceProvider(configurationBackend,
        applicationProperties);
  }

  @Test
  public void loadResources_RmlMappingResourceProvider_WithValidData() throws IOException {
    // Arrange
    InputStream inputStream = new ClassPathResource("/rmlmapping/mapping.trig").getInputStream();
    sailRepository = new SailRepository(new MemoryStore());
    sailRepository.initialize();
    sailRepository.getConnection().add(inputStream, "", RDFFormat.TRIG);
    when(configurationBackend.getRepository()).thenReturn(sailRepository);

    // Act
    rmlMappingResourceProvider.loadResources();

    // Assert
    RmlMapping rmlMapping = rmlMappingResourceProvider.get(DBEERPEDIA.RML_MAPPING);
    assertThat(rmlMapping.getStreamName(), equalTo("stream-Z"));
    assertThat(rmlMapping.getModel().size(), equalTo(14));
  }

  @Test
  public void loadResources_ThrowConfigurationException_WhenRepositoryConnectionError() {
    // Arrange
    sailRepository = new SailRepository(new MemoryStore());
    sailRepository.initialize();
    when(configurationBackend.getRepository()).thenThrow(RepositoryException.class);

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    rmlMappingResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowConfigurationException_WhenSourceNotDefined() throws IOException {
    // Arrange
    InputStream inputStream = new ClassPathResource("/rmlmapping/invalidMapping.trig")
        .getInputStream();
    sailRepository = new SailRepository(new MemoryStore());
    sailRepository.initialize();
    sailRepository.getConnection().add(inputStream, "", RDFFormat.TRIG);
    when(configurationBackend.getRepository()).thenReturn(sailRepository);

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    rmlMappingResourceProvider.loadResources();
  }

  @Test
  public void loadResources_RmlMappingResourceProvider_WithMultipleMappings() throws IOException {
    // Arrange
    InputStream inputStream = new ClassPathResource("/rmlmapping/multipleMappings.trig")
        .getInputStream();
    sailRepository = new SailRepository(new MemoryStore());
    sailRepository.initialize();
    sailRepository.getConnection().add(inputStream, "", RDFFormat.TRIG);
    when(configurationBackend.getRepository()).thenReturn(sailRepository);

    // Act
    rmlMappingResourceProvider.loadResources();

    // Assert
    RmlMapping rmlMapping = rmlMappingResourceProvider.get(DBEERPEDIA.RML_MAPPING);
    assertThat(rmlMapping.getStreamName(), equalTo("stream-Z"));
    assertThat(rmlMapping.getModel().size(), equalTo(14));
    RmlMapping rmlMapping2 = rmlMappingResourceProvider.get(DBEERPEDIA.RML_MAPPING2);
    assertThat(rmlMapping2.getStreamName(), equalTo("stream-B"));
    assertThat(rmlMapping2.getModel().size(), equalTo(12));
  }

}
