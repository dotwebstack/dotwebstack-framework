package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.ShapeProperties;
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
  private ResourcePatternResolver resourceLoader;

  private Rdf4jProperties rdf4jProperties;

  private Rdf4jConfiguration rdf4jConfiguration;

  @BeforeEach
  void setUp() {
    ShapeProperties shapeProperties = new ShapeProperties();
    shapeProperties.setGraph(Constants.SHAPE_GRAPH);
    shapeProperties.setPrefix(Constants.SHAPE_PREFIX);
    rdf4jProperties = new Rdf4jProperties();
    rdf4jProperties.setShape(shapeProperties);
    rdf4jConfiguration = new Rdf4jConfiguration(rdf4jProperties, resourceLoader);
  }

  // @Test
  // void repositoryManager_CreatesEmptyRepository_ForEmptyFolder() throws IOException {
  // when(resourceLoader.getResources(anyString())).thenReturn(new Resource[0]);
  //
  // RepositoryResolver result =
  // rdf4jConfiguration.repositoryManager(rdf4jProperties, configFactory, resourceLoader);
  //
  // @Cleanup
  // RepositoryConnection con = result.getRepository(Rdf4jConfiguration.SHAPE_REPOSITORY_ID)
  // .getConnection();
  // assertThat(con.isEmpty(), is(equalTo(true)));
  // }
  //
  // @Test
  // void repositoryManager_CreatesPopulatedRepository_ForNonEmptyFolder() throws IOException {
  // Resource rdfResource = mock(Resource.class);
  // String rdfContent = "<http://foo> a <http://bar>";
  // when(rdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(rdfContent.getBytes()));
  // when(rdfResource.getFilename()).thenReturn("foo.trig");
  // when(rdfResource.isReadable()).thenReturn(true);
  //
  // Resource nonRdfResource = mock(Resource.class);
  // when(nonRdfResource.getFilename()).thenReturn("foo.txt");
  // when(nonRdfResource.isReadable()).thenReturn(true);
  //
  // Resource folderResource = mock(Resource.class);
  // when(folderResource.isReadable()).thenReturn(false);
  //
  // when(resourceLoader.getResources(anyString()))
  // .thenReturn(new Resource[]{rdfResource, nonRdfResource, folderResource});
  //
  // RepositoryResolver result =
  // rdf4jConfiguration.repositoryManager(rdf4jProperties, configFactory, resourceLoader);
  //
  // @Cleanup
  // RepositoryConnection con = result.getRepository(Rdf4jConfiguration.SHAPE_REPOSITORY_ID)
  // .getConnection();
  // assertThat(con.size(), is(equalTo(1L)));
  // }
  //
  // @Test
  // void repositoryManager_ThrowsException_ForInvalidRdfResource() throws IOException {
  // Resource rdfResource = mock(Resource.class);
  // String rdfContent = "<http://foo> a <http://bar";
  // when(rdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(rdfContent.getBytes()));
  // when(rdfResource.getFilename()).thenReturn("foo.trig");
  // when(rdfResource.isReadable()).thenReturn(true);
  // when(resourceLoader.getResources(anyString())).thenReturn(new Resource[]{rdfResource});
  //
  // assertThrows(RDFParseException.class,
  // () -> rdf4jConfiguration.repositoryManager(rdf4jProperties, configFactory, resourceLoader));
  // }
  //
  // @Test
  // void repositoryManager_ThrowsException_ForWriteError() throws IOException {
  // Resource rdfResource = mock(Resource.class);
  // when(rdfResource.getFilename()).thenReturn("foo.trig");
  // when(rdfResource.isReadable()).thenReturn(true);
  // when(rdfResource.getInputStream()).thenThrow(IOException.class);
  // when(resourceLoader.getResources(anyString())).thenReturn(new Resource[]{rdfResource});
  //
  // assertThrows(UncheckedIOException.class,
  // () -> rdf4jConfiguration.repositoryManager(rdf4jProperties, configFactory, resourceLoader));
  // }
  //
  // @Test
  // void repositoryManager_ThrowsException_ForParseError() throws IOException {
  // when(resourceLoader.getResources(anyString())).thenThrow(IOException.class);
  //
  // assertThrows(UncheckedIOException.class,
  // () -> rdf4jConfiguration.repositoryManager(rdf4jProperties, configFactory, resourceLoader));
  // }

  @Test
  void nodeShapeRegistry_ReturnsRegistry_ForNoShapes() throws IOException {
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[0]);
    var nodeShapeRegistry = rdf4jConfiguration.nodeShapeRegistry();

    assertThat(nodeShapeRegistry.all()
        .isEmpty(), is(equalTo(true)));
  }

  @Test
  void nodeShapeRegistry_ReturnsRegistry_ForShapes() throws IOException {
    when(resourceLoader.getResources(anyString()))
        .thenReturn(new Resource[] {new ClassPathResource("config/shapes/shapes.trig")});
    var nodeShapeRegistry = rdf4jConfiguration.nodeShapeRegistry();

    assertThat(nodeShapeRegistry.get(Constants.BREWERY_SHAPE), is(notNullValue()));
  }
}
