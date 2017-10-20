package org.dotwebstack.framework.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.core.env.Environment;
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
  private Resource elmoConfigurationResource;

  @Mock
  private SailRepositoryConnection repositoryConnection;

  @Mock
  private Environment environment;

  private ResourceLoader resourceLoader;

  private FileConfigurationBackend backend;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    backend = new FileConfigurationBackend(elmoConfigurationResource, repository, "file:config");
    backend.setResourceLoader(resourceLoader);
    backend.setEnvironment(environment);
    when(repository.getConnection()).thenReturn(repositoryConnection);
  }

  @Test
  public void constructor_ThrowsException_WithMissingElmoConfiguration() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new FileConfigurationBackend(null, repository, "file:config");
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepository() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new FileConfigurationBackend(elmoConfigurationResource, null, "file:config");
  }

  @Test
  public void constructor_ThrowsException_WithMissingResourcePath() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new FileConfigurationBackend(elmoConfigurationResource, repository, null);
  }

  @Test
  public void setResourceLoader_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    backend.setResourceLoader(null);
  }

  @Test
  public void setEnvironment_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    backend.setEnvironment(null);
  }

  @Test
  public void setResourceLoader_DoesNotCrash_WithValue() {
    // Act
    backend.setResourceLoader(resourceLoader);
  }

  @Test
  public void setEnvironment_DoesNotCrash_WithValue() {
    // Act
    backend.setEnvironment(environment);
  }

  @Test
  public void loadResources_LoadsRepository_WithConfigTrigFile() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getInputStream()).thenReturn(
        new ByteArrayInputStream("file".getBytes(Charsets.UTF_8)));
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});

    // Act
    backend.loadResources();

    // Assert
    assertThat(backend.getRepository(), equalTo(repository));
    verify(repository).initialize();
    verify(repositoryConnection).add(any(InputStream.class), eq("#"), eq(RDFFormat.TRIG));
    verify(repositoryConnection).close();
    verifyNoMoreInteractions(repositoryConnection);
  }

  @Test
  public void loadResources_LoadsNothing_WhenNoFilesFound() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[0]);

    // Act
    backend.loadResources();

    // Assert
    verifyZeroInteractions(repositoryConnection);
  }

  @Test
  public void loadResources_LoadsNothing_WithIgnoredUnknownFileExtensions() throws IOException {
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
  public void loadResources_ThrowsException_WhenRepositoryConnectionError() throws IOException {
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
  public void loadResources_ThrowsException_WhenRdfDataLoadError() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getInputStream()).thenThrow(new RDFParseException("message"));
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while loading RDF data.");

    // Act
    backend.loadResources();
  }

  @Test
  public void loadResources_LoadsDefaultElmoFile_WhenElmoFileIsPresent() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getInputStream()).thenReturn(
        new ByteArrayInputStream("file".getBytes(Charsets.UTF_8)));
    when(resource.getFilename()).thenReturn("config.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(any())).thenReturn(
        new Resource[] {resource});

    when(elmoConfigurationResource.getInputStream()).thenReturn(
        new ByteArrayInputStream("elmo".getBytes(Charsets.UTF_8)));
    when(elmoConfigurationResource.getFilename()).thenReturn("elmo.trig");

    // Act
    backend.loadResources();

    // Assert
    verify(elmoConfigurationResource, atLeastOnce()).getInputStream();
    ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
    verify(repositoryConnection, times(2)).add(captor.capture(), any(), any());

    List<InputStream> inputStreams = captor.getAllValues();
    List<String> fileContents = inputStreams.stream().map(stream -> {
      try {
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
      } catch (IOException e) {
        fail(e.getMessage());
        return null;
      }
    }).collect(Collectors.toList());

    assertThat(fileContents, hasItems("file", "elmo"));
  }

  @Test
  public void loadPrefixes_ThrowConfigurationException_FoundMultiplePrefixesDeclaration()
      throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getInputStream()).thenReturn(
        new ByteArrayInputStream(new String("@prefix dbeerpedia: <http://dbeerpedia.org#> .\n"
            + "@prefix elmo: <http://dotwebstack.org/def/elmo#> .\n"
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
            + "@prefix rdfs: <http://www.have-a-nice-day.com/rdf-schema#> .")
            .getBytes(Charsets.UTF_8)));
    when(resource.getFilename()).thenReturn("_prefixes.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(any())).thenReturn(
        new Resource[] {resource});

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        "Found multiple declaration <@prefix rdfs: <http://www.have-a-nice-day.com/rdf-schema#> .> at line <5>");

    // Act
    backend.loadResources();
  }

  @Test
  public void loadPrefixes_ThrowConfigurationException_FoundUnknownPrefix() throws Exception {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getInputStream()).thenReturn(
        new ByteArrayInputStream(new String("@prefix dbeerpedia: <http://dbeerpedia.org#> .\n"
            + "@prefix elmo: <http://dotwebstack.org/def/elmo#> .\n"
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
            + "this is not a valid prefix")
            .getBytes(Charsets.UTF_8)));
    when(resource.getFilename()).thenReturn("_prefixes.trig");
    when(((ResourcePatternResolver) resourceLoader).getResources(any())).thenReturn(
        new Resource[] {resource});

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        "Found unknown prefix format <this is not a valid prefix> at line <5>");

    // Act
    backend.loadResources();
  }
}
