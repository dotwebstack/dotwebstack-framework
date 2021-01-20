package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.Cleanup;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.ShapeProperties;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResolver;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class Rdf4jConfigurationTest {

  @Mock
  private ConfigFactory configFactory;

  @Mock
  private ResourcePatternResolver resourceLoader;

  private final Rdf4jConfiguration rdf4jConfiguration = new Rdf4jConfiguration();

  private Rdf4jProperties rdf4jProperties;

  @BeforeEach
  void setUp() {
    ShapeProperties shapeProperties = new ShapeProperties();
    shapeProperties.setGraph(Constants.SHAPE_GRAPH);
    shapeProperties.setPrefix(Constants.SHAPE_PREFIX);
    rdf4jProperties = new Rdf4jProperties();
    rdf4jProperties.setShape(shapeProperties);
  }

  @Test
  void configFactory_ReturnsConfigFactoryImpl_ForAnyCall() {
    // Act
    ConfigFactory result = rdf4jConfiguration.configFactory();

    // Assert
    assertThat(result, is(instanceOf(ConfigFactoryImpl.class)));
  }

  @Test
  void repositoryManager_CreatesEmptyRepository_ForEmptyFolder() throws IOException {
    // Arrange
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[0]);

    // Act
    RepositoryResolver result =
        rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader);

    // Assert
    @Cleanup
    RepositoryConnection con = result.getRepository(Rdf4jConfiguration.LOCAL_REPOSITORY_ID)
        .getConnection();
    assertThat(con.isEmpty(), is(equalTo(true)));
  }

  @Test
  void repositoryManager_CreatesPopulatedRepository_ForNonEmptyFolder() throws IOException {
    // Arrange
    Resource rdfResource = mock(Resource.class);
    String rdfContent = "<http://foo> a <http://bar>";
    when(rdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(rdfContent.getBytes()));
    when(rdfResource.getFilename()).thenReturn("foo.trig");
    when(rdfResource.isReadable()).thenReturn(true);

    Resource nonRdfResource = mock(Resource.class);
    when(nonRdfResource.getFilename()).thenReturn("foo.txt");
    when(nonRdfResource.isReadable()).thenReturn(true);

    Resource folderResource = mock(Resource.class);
    when(folderResource.isReadable()).thenReturn(false);

    when(resourceLoader.getResources(anyString()))
        .thenReturn(new Resource[] {rdfResource, nonRdfResource, folderResource});

    // Act
    RepositoryResolver result =
        rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader);

    // Assert
    @Cleanup
    RepositoryConnection con = result.getRepository(Rdf4jConfiguration.LOCAL_REPOSITORY_ID)
        .getConnection();
    assertThat(con.size(), is(equalTo(1L)));
  }

  @Test
  void repositoryManager_ThrowsException_ForInvalidRdfResource() throws IOException {
    // Arrange
    Resource rdfResource = mock(Resource.class);
    String rdfContent = "<http://foo> a <http://bar";
    when(rdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(rdfContent.getBytes()));
    when(rdfResource.getFilename()).thenReturn("foo.trig");
    when(rdfResource.isReadable()).thenReturn(true);
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {rdfResource});

    // Act / Assert
    assertThrows(RDFParseException.class,
        () -> rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader));
  }

  @Test
  void repositoryManager_ThrowsException_ForWriteError() throws IOException {
    // Arrange
    Resource rdfResource = mock(Resource.class);
    when(rdfResource.getFilename()).thenReturn("foo.trig");
    when(rdfResource.isReadable()).thenReturn(true);
    when(rdfResource.getInputStream()).thenThrow(IOException.class);
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {rdfResource});

    // Act / Assert
    assertThrows(UncheckedIOException.class,
        () -> rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader));
  }

  @Test
  void repositoryManager_ThrowsException_ForParseError() throws IOException {
    // Arrange
    when(resourceLoader.getResources(anyString())).thenThrow(IOException.class);

    // Act / Assert
    assertThrows(UncheckedIOException.class,
        () -> rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader));
  }

  @Test
  void nodeShapeRegistry_ReturnsRegistry_ForNoShapes() throws IOException {
    // Arrange
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[0]);
    LocalRepositoryManager localRepositoryManager =
        rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader);

    // Act
    NodeShapeRegistry nodeShapeRegistry = rdf4jConfiguration.nodeShapeRegistry(localRepositoryManager, rdf4jProperties);

    // Assert
    assertThat(nodeShapeRegistry.all()
        .isEmpty(), is(equalTo(true)));
  }

  @Test
  void nodeShapeRegistry_ReturnsRegistry_ForShapes() throws IOException {
    // Arrange
    when(resourceLoader.getResources(anyString()))
        .thenReturn(new Resource[] {new ClassPathResource("config/model/shapes.trig")});
    LocalRepositoryManager localRepositoryManager =
        rdf4jConfiguration.localRepositoryManager(rdf4jProperties, configFactory, resourceLoader);

    // Act
    NodeShapeRegistry nodeShapeRegistry = rdf4jConfiguration.nodeShapeRegistry(localRepositoryManager, rdf4jProperties);

    // Assert
    assertThat(nodeShapeRegistry.get(Constants.BREWERY_SHAPE), is(notNullValue()));
  }
}
