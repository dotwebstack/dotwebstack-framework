package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.Cleanup;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties.ShapeProperties;
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
  private ResourcePatternResolver resourceLoader;

  private Rdf4jConfiguration rdf4jConfiguration;

  @BeforeEach
  void setUp() {
    var shapeProperties = new ShapeProperties();
    shapeProperties.setGraph(Constants.SHAPE_GRAPH);
    shapeProperties.setPrefix(Constants.SHAPE_PREFIX);

    var rdf4jProperties = new Rdf4jProperties();
    rdf4jProperties.setShape(shapeProperties);
    rdf4jConfiguration = new Rdf4jConfiguration(rdf4jProperties, resourceLoader);
  }

  @Test
  void repository_CreatesEmptyRepository_ForEmptyFolder() throws IOException {
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[0]);
    var repository = rdf4jConfiguration.repository();

    @Cleanup
    var conn = repository.getConnection();
    assertThat(conn.isEmpty(), is(equalTo(true)));
  }

  @Test
  void repository_CreatesPopulatedRepository_ForNonEmptyFolder() throws IOException {
    var rdfResource = mock(Resource.class);
    var rdfContent = "<http://foo> a <http://bar>";

    when(rdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(rdfContent.getBytes()));
    when(rdfResource.isFile()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {rdfResource});

    var repository = rdf4jConfiguration.repository();

    @Cleanup
    var con = repository.getConnection();
    assertThat(con.size(), is(equalTo(1L)));
  }

  @Test
  void repository_ThrowsException_ForInvalidRdfResource() throws IOException {
    var rdfResource = mock(Resource.class);
    var rdfContent = "<http://foo> a <http://bar";

    when(rdfResource.getInputStream()).thenReturn(new ByteArrayInputStream(rdfContent.getBytes()));
    when(rdfResource.isFile()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {rdfResource});

    assertThrows(RDFParseException.class, () -> rdf4jConfiguration.repository());
  }

  @Test
  void repository_ThrowsException_ForWriteError() throws IOException {
    Resource rdfResource = mock(Resource.class);
    when(rdfResource.isFile()).thenReturn(true);
    when(rdfResource.getInputStream()).thenThrow(IOException.class);
    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {rdfResource});

    assertThrows(IOException.class, () -> rdf4jConfiguration.repository());
  }

  @Test
  void repository_ThrowsException_ForParseError() throws IOException {
    when(resourceLoader.getResources(anyString())).thenThrow(IOException.class);

    assertThrows(IOException.class, () -> rdf4jConfiguration.repository());
  }

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
