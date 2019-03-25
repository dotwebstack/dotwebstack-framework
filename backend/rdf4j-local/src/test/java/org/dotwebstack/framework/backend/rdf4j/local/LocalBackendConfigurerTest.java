package org.dotwebstack.framework.backend.rdf4j.local;

import static org.dotwebstack.framework.backend.rdf4j.local.LocalBackend.LOCAL_BACKEND_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.dotwebstack.framework.core.Backend;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

class LocalBackendConfigurerTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private LocalBackendConfigurer configurer;

  private ResourceLoader resourceLoader;

  private BackendRegistry backendRegistry;

  @BeforeEach
  void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    configurer = new LocalBackendConfigurer(resourceLoader);
    backendRegistry = new BackendRegistry();
  }

  @Test
  void repositoryConnection_ThrowsException_ForMissingFolder() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString()))
        .thenThrow(new IOException());

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        configurer.registerBackends(backendRegistry));
  }

  @Test
  void repositoryConnection_ReturnsEmptyRepository_ForNonRdfResources() throws IOException {
    // Arrange
    Resource file = createFileResource("foo.txt", "foo");
    Resource nameless = createFileResource(null, "bar");
    Resource directory = createDirectoryResource("foo.trig");
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString()))
        .thenReturn(new Resource[]{file, nameless, directory});

    // Act
    configurer.registerBackends(backendRegistry);

    // Assert
    Backend localBackend = backendRegistry.get(LOCAL_BACKEND_NAME);
    assertThat(localBackend, is(instanceOf(LocalBackend.class)));
    SailRepository repository = ((LocalBackend) localBackend).getRepository();
    assertThat(repository.getConnection().isEmpty(), is(equalTo(true)));
  }

  @Test
  void repositoryConnection_ReturnsPopulatedRepository_ForRdfResources() throws IOException {
    // Arrange
    Resource firstFile = createFileResource("first.trig", "<foo:1> a rdfs:Class");
    Resource secondFile = createFileResource("second.trig", "<foo:2> a rdfs:Class");
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString()))
        .thenReturn(new Resource[]{firstFile, secondFile});

    // Act
    configurer.registerBackends(backendRegistry);

    // Assert
    Backend localBackend = backendRegistry.get(LOCAL_BACKEND_NAME);
    assertThat(localBackend, is(instanceOf(LocalBackend.class)));
    SailRepository repository = ((LocalBackend) localBackend).getRepository();
    List<Statement> result = QueryResults
        .asList(repository.getConnection().getStatements(null, null, null));
    assertThat(result.size(), is(equalTo(2)));
    assertThat(result, hasItems(
        VF.createStatement(VF.createIRI("foo:1"), RDF.TYPE, RDFS.CLASS),
        VF.createStatement(VF.createIRI("foo:2"), RDF.TYPE, RDFS.CLASS)));
  }

  @Test
  void repositoryConnection_ThrowsException_ForInvalidSyntax() throws IOException {
    // Arrange
    Resource file = createFileResource("foo.trig", "foo");
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString()))
        .thenReturn(new Resource[]{file});

    // Act / Assert
    assertThrows(RDFParseException.class, () ->
        configurer.registerBackends(backendRegistry));
  }

  @Test
  void repositoryConnection_ThrowsException_ForReadError() throws IOException {
    // Arrange
    Resource file = createFileResource("foo.trig", "foo");
    when(file.getInputStream()).thenThrow(new IOException());
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString()))
        .thenReturn(new Resource[]{file});

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        configurer.registerBackends(backendRegistry));
  }

  private static Resource createFileResource(String name, String contents) throws IOException {
    Resource file = mock(Resource.class);

    when(file.isReadable()).thenReturn(true);
    when(file.getFilename()).thenReturn(name);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(contents.getBytes()));

    return file;
  }

  private static Resource createDirectoryResource(String name) {
    Resource directory = mock(Resource.class);

    when(directory.isReadable()).thenReturn(false);
    when(directory.getFilename()).thenReturn(name);

    return directory;
  }

}
