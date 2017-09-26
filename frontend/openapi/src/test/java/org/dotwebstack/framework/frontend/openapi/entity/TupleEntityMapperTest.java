package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TupleEntityMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SchemaMapperAdapter schemaHandler;

  @Mock
  private TupleQueryResult result;

  private TupleEntityMapper tupleEntityMapper;

  @Before
  public void setUp() {
    tupleEntityMapper = new TupleEntityMapper(schemaHandler);
  }

  @Test
  public void constructor_ThrowsException_WithMissingSchemaHandlerAdapter() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TupleEntityMapper(null);
  }

  @Test
  public void map_ThrowsException_WithMissingTupleEntity() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    tupleEntityMapper.map(null, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void map_ThrowsException_WithMissingMediaType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    tupleEntityMapper.map(mock(TupleEntity.class), null);
  }

  @Test
  public void map_ThrowsException_ForUnknownMediaType() {
    // Arrange
    TupleEntity entity = new TupleEntity(
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new ObjectProperty()), result);

    // Assert
    thrown.expect(EntityMapperRuntimeException.class);
    thrown.expectMessage(String.format("No schema found for media type '%s'.",
        MediaType.TEXT_PLAIN_TYPE.toString()));

    // Act
    tupleEntityMapper.map(entity, MediaType.TEXT_PLAIN_TYPE);
  }

  @Test
  public void map_ReturnsEmptyMap_ForNonArraySchema() {
    // Assert
    TupleEntity entity = new TupleEntity(
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new ObjectProperty()), result);

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, equalTo(ImmutableMap.of()));
  }

  @Test
  public void map_ThrowsError_ForArraySchemaWithMissingItemsProperty() {
    // Assert
    TupleEntity entity = new TupleEntity(
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new ArrayProperty()), result);

    // Assert
    thrown.expect(EntityMapperRuntimeException.class);
    thrown.expectMessage("Array schemas must have an 'items' property.");

    // Act
    tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void map_ThrowsError_ForArraySchemaWithNonObjectItemSchema() {
    // Assert
    TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ArrayProperty().items(new StringProperty())), mock(TupleQueryResult.class));

    // Assert
    thrown.expect(EntityMapperRuntimeException.class);
    thrown.expectMessage("Only array items of type 'object' are supported for now.");

    // Act
    tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void map_MapsToHandlerResult_ForRequiredPropertyWithPresentBinding() {
    // Assert
    StringProperty nameProperty = new StringProperty().required(true);
    TupleEntity entity = new TupleEntity(
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new ArrayProperty().items(
            new ObjectProperty().required(true).properties(ImmutableMap.of("name", nameProperty)))),
        result);
    when(result.hasNext()).thenReturn(true, false);
    when(result.next()).thenReturn(
        new ListBindingSet(ImmutableList.of("name"), ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME)));
    when(schemaHandler.mapTupleValue(nameProperty, DBEERPEDIA.BROUWTOREN_NAME)).thenReturn(
        DBEERPEDIA.BROUWTOREN_NAME.stringValue());

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, instanceOf(ImmutableList.class));
    assertThat((ImmutableList<Object>) mappedEntity, hasSize(1));
    assertThat((ImmutableList<Object>) mappedEntity,
        contains(ImmutableMap.of("name", DBEERPEDIA.BROUWTOREN_NAME.stringValue())));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void map_MapsToAbsentOptionalValue_ForOptionalPropertyWithAbsentBinding() {
    // Assert
    TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ArrayProperty().items(new ObjectProperty().properties(
            ImmutableMap.of("name", new StringProperty().required(false))))),
        result);
    when(result.hasNext()).thenReturn(true, false);
    when(result.next()).thenReturn(new ListBindingSet(ImmutableList.of(), ImmutableList.of()));

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, instanceOf(ImmutableList.class));
    assertThat((ImmutableList<Object>) mappedEntity, hasSize(1));
    assertThat((ImmutableList<Object>) mappedEntity,
        contains(ImmutableMap.of("name", Optional.absent())));
  }

  @Test
  public void map_ThrowException_ForRequiredPropertyWithAbsentBinding() {
    // Assert
    TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ArrayProperty().items(new ObjectProperty().properties(
            ImmutableMap.of("name", new StringProperty().required(true))))),
        result);
    when(result.hasNext()).thenReturn(true, false);
    when(result.next()).thenReturn(new ListBindingSet(ImmutableList.of(), ImmutableList.of()));

    // Assert
    thrown.expect(EntityMapperRuntimeException.class);
    thrown.expectMessage("Property 'name' is required.");

    // Act
    tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);
  }

}
