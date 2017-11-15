package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.models.properties.Property;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilder;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.properties.PropertyHandlerRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
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
  public void map_GraphMapping() {
    // Arrange
    Property property = mock(Property.class);
    QueryResult queryResult = mock(QueryResult.class);
    Model model = new ModelBuilder().add("http://www.test.nl", "http://www.test.nl",
        "http://www.test.nl/").build();
    when(queryResult.getModel()).thenReturn(model);
    GraphEntity entity = new GraphEntity(property, queryResult);

    // Assert
    Map<String, Object> mapTest = new HashMap<>();
    mapTest.put("test", "test");
    when(entityBuilder.build(any(), eq(propertyHandlerRegistry),
        argThat(f -> f.getQueryResult().equals(queryResult)))).thenReturn(mapTest);

    // Act
    Object mappedEntity = graphEntityMapper.map(entity, MediaType.TEXT_PLAIN_TYPE);
    assertThat(mappedEntity, instanceOf(HashMap.class));
    Map<String, Object> map = (Map<String, Object>) mappedEntity;
    assertThat(map.values(), hasSize(1));
    assertThat(map.get("test"), equalTo("test"));
  }

}
