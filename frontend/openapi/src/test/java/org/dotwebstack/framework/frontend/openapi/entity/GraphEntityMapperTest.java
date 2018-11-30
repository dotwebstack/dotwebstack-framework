package org.dotwebstack.framework.frontend.openapi.entity;

import static org.dotwebstack.framework.frontend.openapi.entity.GraphEntity.newGraphEntity;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphEntityMapperTest {

  @Mock
  private OpenAPI openApiMock;

  @Mock
  private Repository repositoryMock;

  @Mock
  private RequestContext requestContextMock;

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
    IntegerSchema schema = new IntegerSchema();
    GraphEntity entity = newGraphEntity(
        new ApiResponse().content(new Content().addMediaType(MediaType.TEXT_PLAIN_TYPE.toString(),
            new io.swagger.v3.oas.models.media.MediaType().schema(schema))),
        repositoryMock, ImmutableSet.of(), openApiMock, requestContextMock);

    Object object = new Object();
    when(schemaMapperAdapterMock.mapGraphValue(any(Schema.class), anyBoolean(),
        any(GraphEntity.class), any(ValueContext.class),
        any(SchemaMapperAdapter.class))).thenReturn(object);

    // Act
    Object result = entityMapper.map(entity, MediaType.TEXT_PLAIN_TYPE);

    // Assert
    assertThat(result, sameInstance(object));
  }

}
