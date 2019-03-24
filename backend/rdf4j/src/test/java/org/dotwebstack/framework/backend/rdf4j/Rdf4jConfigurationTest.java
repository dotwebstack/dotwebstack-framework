package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
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
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

class Rdf4jConfigurationTest {

  private static final ValueFactory vf = SimpleValueFactory.getInstance();

  private Rdf4jConfiguration rdf4jConfiguration = new Rdf4jConfiguration();

  private ResourceLoader resourceLoader;

  @BeforeEach
  void setUp() {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
  }

  @Test
  void repositoryConnection_ThrowsException_ForMissingFolder() throws IOException {
    // Arrange
    when(((ResourcePatternResolver) resourceLoader)
        .getResources(anyString()))
        .thenThrow(new IOException());

    // Act / Assert
    assertThrows(IOException.class, () ->
        rdf4jConfiguration.repositoryConnection(resourceLoader));
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
    RepositoryConnection repositoryConnection = rdf4jConfiguration
        .repositoryConnection(resourceLoader);

    // Assert
    assertThat(repositoryConnection.isEmpty(), is(equalTo(true)));
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
    RepositoryConnection repositoryConnection = rdf4jConfiguration
        .repositoryConnection(resourceLoader);

    // Assert
    List<Statement> result = QueryResults
        .asList(repositoryConnection.getStatements(null, null, null));
    assertThat(result.size(), is(equalTo(2)));
    assertThat(result, hasItems(
        vf.createStatement(vf.createIRI("foo:1"), RDF.TYPE, RDFS.CLASS),
        vf.createStatement(vf.createIRI("foo:2"), RDF.TYPE, RDFS.CLASS)));
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
        rdf4jConfiguration.repositoryConnection(resourceLoader));
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
        rdf4jConfiguration.repositoryConnection(resourceLoader));
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
