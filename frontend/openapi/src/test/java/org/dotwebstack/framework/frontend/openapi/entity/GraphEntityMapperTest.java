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

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.schema.SchemaMapperAdapter;
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
  private GraphEntityContext graphEntityContext;

  @Mock
  private SchemaMapperAdapter propertyHandlerRegistry;

  @Before
  public void setUp() {
    graphEntityMapper = new GraphEntityMapper(propertyHandlerRegistry);
  }

  @Test
  public void constructor_ThrowsException_WithMissingDeps() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GraphEntityMapper( null);
  }

  @Test
  public void map_ThrowsException_WithMissingGraphEntity() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    graphEntityMapper.mapGraph(null, null,graphEntityContext);
  }

  @Test
  public void map_GraphMapping() {
    // Arrange
    Property property = mock(Property.class);
    QueryResult queryResult = mock(QueryResult.class);
    GraphEntity entity = new GraphEntity(property, queryResult, ImmutableMap.of(),ImmutableMap.of());

    when(propertyHandlerRegistry.mapGraphValue(any(),any(),any(),any())).thenReturn(new HashMap<>());
    //Act
    Object mappedEntity = graphEntityMapper.mapGraph(entity, MediaType.TEXT_PLAIN_TYPE,graphEntityContext);
    //Assert
    assertThat(mappedEntity, instanceOf(HashMap.class));

  }

}
