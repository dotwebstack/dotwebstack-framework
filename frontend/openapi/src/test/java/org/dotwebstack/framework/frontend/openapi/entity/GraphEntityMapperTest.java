package org.dotwebstack.framework.frontend.openapi.entity;

import static org.mockito.Mockito.mock;

import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilder;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.properties.PropertyHandlerRegistry;
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
  private EntityBuilder entityBuilder;

  @Mock
  private PropertyHandlerRegistry propertyHandlerRegistry;

  @Before
  public void setUp() {
    graphEntityMapper = new GraphEntityMapper(entityBuilder, propertyHandlerRegistry);
  }

  @Test
  public void constructor_ThrowsException_WithMissingDeps() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GraphEntityMapper(null, null);
  }

  @Test
  public void map_ThrowsException_WithMissingGraphEntity() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    graphEntityMapper.map(null, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void map_ThrowsException_ForUnknownMediaType() {
    // Arrange
    Property property = mock(Property.class);
    QueryResult queryResult = mock(QueryResult.class);
    GraphEntity entity = new GraphEntity(property, queryResult, "", "");

    // Assert
    thrown.expect(EntityMapperRuntimeException.class);
    thrown.expectMessage(String.format("No schema found for media type '%s'.",
        MediaType.TEXT_PLAIN_TYPE.toString()));

    // Act
    graphEntityMapper.map(entity, MediaType.TEXT_PLAIN_TYPE);
  }

}
