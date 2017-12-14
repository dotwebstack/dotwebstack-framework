package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.IntegerProperty;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphEntityMapperTest {

  @Mock
  private GraphEntityContext contextMock;

  @Mock
  private SchemaMapperAdapter schemaMapperAdapterMock;

  private GraphEntityMapper entityMapper;

  @Before
  public void setUp() {
    entityMapper = new GraphEntityMapper(schemaMapperAdapterMock);
  }

  @Test
  public void map_Returns_SchemaMapperAdapterResult() {
    // Arrange
    IntegerProperty schema = new IntegerProperty();
    GraphEntity entity =
        new GraphEntity(ImmutableMap.of(MediaType.TEXT_PLAIN_TYPE, schema), contextMock);

    Object object = new Object();
    when(schemaMapperAdapterMock.mapGraphValue(any(IntegerProperty.class),
        any(GraphEntityContext.class), any(ValueContext.class),
        any(SchemaMapperAdapter.class))).thenReturn(object);

    // Act
    Object result = entityMapper.map(entity, MediaType.TEXT_PLAIN_TYPE);

    // Assert
    assertThat(result, sameInstance(object));
  }

}
