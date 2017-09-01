package org.dotwebstack.framework.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.dotwebstack.framework.EnvVariableParser;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class FileConfigurationBackendTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SailRepository repository;

  @Mock
  private Resource elmoConfiguration;

  @Mock
  private SailRepositoryConnection repositoryConnection;

  @Mock
  private EnvVariableParser envVariableParser;

  private ResourceLoader resourceLoader;

  private FileConfigurationBackend backend;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    backend = new FileConfigurationBackend(elmoConfiguration, envVariableParser, repository);
    backend.setResourceLoader(resourceLoader);
    when(repository.getConnection()).thenReturn(repositoryConnection);
  }

  @Test
  public void loadTrigFile() throws IOException {
    // Arrange
    Resource resource = mock(Resource.class);
    InputStream resourceInputStream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(resourceInputStream);
    when(envVariableParser.parse(any(InputStream.class))).thenReturn(resourceInputStream);
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});

    // Act
    backend.loadResources();

    // Assert
    assertThat(backend.getRepository(), equalTo(repository));
    verify(repository).initialize();
    verify(repositoryConnection).add(eq(resourceInputStream), eq("#"), eq(RDFFormat.TRIG));
    verify(repositoryConnection).close();
    verifyNoMoreInteractions(repositoryConnection);
  }

  @Test
  public void doNothingWhenNoFilesFound() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[0]);

    // Act
    backend.loadResources();

    // Assert
    verifyZeroInteractions(repositoryConnection);
  }

  @Test
  public void ignoreUnknownFileExtension() throws IOException {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getFilename()).thenReturn("not-existing.md");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});

    // Act
    backend.loadResources();

    // Assert
    verify(repositoryConnection).close();
    verifyNoMoreInteractions(repositoryConnection);
  }

  @Test
  public void repositoryConnectionError() throws IOException {
    // Arrange
    Resource resource = mock(Resource.class);
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});
    when(repository.getConnection()).thenThrow(RepositoryException.class);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while getting repository connection.");

    // Act
    backend.loadResources();
  }

  @Test
  public void dataLoadError() throws IOException {
    // Arrange
    Resource resource = mock(Resource.class);
    InputStream resourceInputStream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(resourceInputStream);
    when(envVariableParser.parse(any(InputStream.class))).thenReturn(resourceInputStream);
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});
    doThrow(RDFParseException.class).when(repositoryConnection).add(resourceInputStream, "#",
        RDFFormat.TRIG);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while loading RDF data.");

    // Act
    backend.loadResources();
  }

  @Test
  public void loadsDefaultElmoResource() throws IOException {
    // Arrange
    Resource resource = mock(Resource.class);
    InputStream resourceInputStream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(resourceInputStream);
    when(envVariableParser.parse(resourceInputStream)).thenReturn(resourceInputStream);
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(any())).thenReturn(
        new Resource[] {resource});

    InputStream elmoInputStream = mock(InputStream.class);
    when(elmoConfiguration.getInputStream()).thenReturn(elmoInputStream);
    when(envVariableParser.parse(elmoInputStream)).thenReturn(elmoInputStream);
    when(elmoConfiguration.getFilename()).thenReturn("elmo.trig");

    // Act
    backend.loadResources();

    // Assert
    verify(elmoConfiguration, atLeastOnce()).getInputStream();
    ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
    verify(repositoryConnection, times(2)).add(captor.capture(), any(), any());

    List<InputStream> inputStreams = captor.getAllValues();
    assertThat(inputStreams, contains(resourceInputStream, elmoInputStream));
  }
}
