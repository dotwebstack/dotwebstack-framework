package org.dotwebstack.framework.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.List;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class FileConfigurationBackendTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private ResourceLoader resourceLoader;

  private FileConfigurationBackend backend;

  @Before
  public void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    backend = new FileConfigurationBackend();
    backend.setResourceLoader(resourceLoader);
  }

  @Test
  public void getRepositoryWhenNotInitialized() {
    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Repository cannot be retrieved until it has been initialized.");

    // Act
    backend.getRepository();
  }

  @Test
  public void loadTurtleFile() throws IOException {
    // Arrange
    Resource resource = new ClassPathResource("model/dbpeerpedia.ttl");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});

    // Act
    backend.initialize();
    SailRepository repository = backend.getRepository();

    // Assert
    List<Statement> statements =
        Iterations.asList(repository.getConnection().getStatements(null, null, null));
    assertThat(repository.isInitialized(), equalTo(true));
    assertThat(statements, not(empty()));
  }

  @Test
  public void doNothingWhenNoFilesFound() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[0]);

    // Act
    backend.initialize();
    SailRepository repository = backend.getRepository();

    // Assert
    List<Statement> statements =
        Iterations.asList(repository.getConnection().getStatements(null, null, null));
    assertThat(repository.isInitialized(), equalTo(true));
    assertThat(statements, empty());
  }

  @Test
  public void ignoreUnknownFileExtension() throws IOException {
    // Arrange
    Resource resource = mock(Resource.class);
    when(resource.getFilename()).thenReturn("not-existing.md");
    when(((ResourcePatternResolver) resourceLoader).getResources(anyString())).thenReturn(
        new Resource[] {resource});

    // Act
    backend.initialize();
    SailRepository repository = backend.getRepository();

    // Assert
    List<Statement> statements =
        Iterations.asList(repository.getConnection().getStatements(null, null, null));
    assertThat(repository.isInitialized(), equalTo(true));
    assertThat(statements, empty());
  }

}
