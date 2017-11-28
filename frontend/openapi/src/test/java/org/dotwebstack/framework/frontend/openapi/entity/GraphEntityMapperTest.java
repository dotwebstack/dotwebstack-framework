package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphEntityMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private GraphEntityMapper graphEntityMapper;

  @Mock
  private GraphEntityContext graphEntityContextMock;

  @Mock
  private SchemaMapperAdapter propertyHandlerRegistryMock;

  @Before
  public void setUp() {
    graphEntityMapper = new GraphEntityMapper(propertyHandlerRegistryMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingDeps() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GraphEntityMapper(null);
  }

  @Test
  public void map_ThrowsException_WithMissingGraphEntity() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    graphEntityMapper.map(null, null);
  }

  @Test
  public void map_GraphMapping() {
    // Arrange
    Map<MediaType, Property> schemaMap = new HashMap<>();
    schemaMap.put(MediaType.TEXT_PLAIN_TYPE,new IntegerProperty());
    GraphEntity entity = new GraphEntity(schemaMap, graphEntityContextMock);

    when(propertyHandlerRegistryMock.mapGraphValue(any(), any(), any(), any())).thenReturn(
        new HashMap<>());
    // Act
    Object mappedEntity = graphEntityMapper.map(entity, MediaType.TEXT_PLAIN_TYPE);
    // Assert
    assertThat(mappedEntity, instanceOf(HashMap.class));

  }

}
