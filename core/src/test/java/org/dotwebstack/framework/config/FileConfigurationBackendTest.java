package org.dotwebstack.framework.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.dotwebstack.framework.EnvironmentAwareResource;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FileConfigurationBackend.class, EnvironmentAwareResource.class, InputStream.class})
public class FileConfigurationBackendTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SailRepository repository;

  @Mock
  private Resource elmoConfigurationResource;

  @Mock
  private SailRepositoryConnection repositoryConnection;

  @Mock
  private InputStream environmentAwareInputStream;

  private ResourceLoader resourceLoader;

  private FileConfigurationBackend backend;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    backend = new FileConfigurationBackend(elmoConfigurationResource, repository,
        "file:.");
    backend.setResourceLoader(resourceLoader);
    when(repository.getConnection()).thenReturn(repositoryConnection);
  }

  @Test
  public void loadTrigFile() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    EnvironmentAwareResource environmentAwareResource = mock(EnvironmentAwareResource.class);
    whenNew(EnvironmentAwareResource.class).withAnyArguments().thenReturn(environmentAwareResource);
    when(environmentAwareResource.getInputStream()).thenReturn(environmentAwareInputStream);
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[]{resource});

    // Act
    backend.loadResources();

    // Assert
    assertThat(backend.getRepository(), equalTo(repository));
    verify(repository).initialize();
    verify(repositoryConnection).add(environmentAwareInputStream, "#", RDFFormat.TRIG);

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
        new Resource[]{resource});

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
        new Resource[]{resource});
    when(repository.getConnection()).thenThrow(RepositoryException.class);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while getting repository connection.");

    // Act
    backend.loadResources();
  }

  @Test
  public void dataLoadError() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    EnvironmentAwareResource environmentAwareResource = mock(EnvironmentAwareResource.class);
    whenNew(EnvironmentAwareResource.class).withAnyArguments().thenReturn(environmentAwareResource);
    when(environmentAwareResource.getInputStream()).thenReturn(environmentAwareInputStream);
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[]{resource});
    doThrow(RDFParseException.class).when(repositoryConnection)
        .add(environmentAwareInputStream, "#", RDFFormat.TRIG);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while loading RDF data.");

    // Act
    backend.loadResources();
  }

  @Test
  public void loadsDefaultElmoResource() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    InputStream rawInputStream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(rawInputStream);
    InputStream environmentAwareInputStream = mock(InputStream.class);
    EnvironmentAwareResource environmentAwareResource = mock(EnvironmentAwareResource.class);
    whenNew(EnvironmentAwareResource.class).withArguments(rawInputStream)
        .thenReturn(environmentAwareResource);
    when(environmentAwareResource.getInputStream()).thenReturn(environmentAwareInputStream);

    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(any())).thenReturn(
        new Resource[]{resource});

    InputStream rawElmoInputStream = mock(InputStream.class);
    when(elmoConfigurationResource.getInputStream()).thenReturn(rawElmoInputStream);
    InputStream environmentAwareElmoInputStream = mock(InputStream.class);
    EnvironmentAwareResource environmentAwareElmoResource = mock(EnvironmentAwareResource.class);
    whenNew(EnvironmentAwareResource.class)
        .withArguments(rawElmoInputStream)
        .thenReturn(environmentAwareElmoResource);
    when(environmentAwareElmoResource.getInputStream()).thenReturn(environmentAwareElmoInputStream);

    when(elmoConfigurationResource.getFilename()).thenReturn("elmo.trig");

    // Act
    backend.loadResources();

    // Assert
    verify(elmoConfigurationResource, atLeastOnce()).getInputStream();
    ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
    verify(repositoryConnection, times(2))
        .add(captor.capture(), any(), any());

    List<InputStream> inputStreams = captor.getAllValues();
    assertThat(inputStreams,
        contains(environmentAwareInputStream, environmentAwareElmoInputStream));
  }
}
