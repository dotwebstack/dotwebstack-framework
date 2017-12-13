package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Response;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ResponseProperty;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
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
  private SchemaMapperAdapter schemaMapper;

  @Mock
  private TupleQueryResult result;

  private TupleEntityMapper tupleEntityMapper;

  @Before
  public void setUp() {
    tupleEntityMapper = new TupleEntityMapper(schemaMapper);
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
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new DoubleProperty()), result);

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
  public void map_MapsToMapperResult_ForRequiredPropertyWithPresentBinding() {
    // Assert
    StringProperty nameProperty = new StringProperty().required(true);
    TupleEntity entity = new TupleEntity(
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new ArrayProperty().items(
            new ObjectProperty().required(true).properties(ImmutableMap.of("name", nameProperty)))),
        result);
    when(result.hasNext()).thenReturn(true, false);
    when(result.next()).thenReturn(
        new ListBindingSet(ImmutableList.of("name"), ImmutableList.of(DBEERPEDIA.BROUWTOREN_NAME)));
    when(schemaMapper.mapTupleValue(any(StringProperty.class),
        any(ValueContext.class))).thenReturn(DBEERPEDIA.BROUWTOREN_NAME.stringValue());

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, instanceOf(ImmutableList.class));
    assertThat((ImmutableList<Object>) mappedEntity, hasSize(1));
    assertThat((ImmutableList<Object>) mappedEntity,
        contains(ImmutableMap.of("name", DBEERPEDIA.BROUWTOREN_NAME.stringValue())));
  }

  @Test
  public void map_MapsToAbsentOptionalValue_ForOptionalPropertyWithAbsentBinding() {
    // Arrange
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
  public void map_MapsToObjectProperty_ForSingleResult() {
    // Arrange
    StringProperty stringProperty = new StringProperty();
    final TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ObjectProperty().properties(ImmutableMap.of("name", stringProperty.required(false)))),
        result);
    when(result.hasNext()).thenReturn(true, false);
    QueryBindingSet bindingSet = new QueryBindingSet();
    bindingSet.addBinding("name", DBEERPEDIA.BROUWTOREN_NAME);
    when(result.next()).thenReturn(bindingSet);
    when(schemaMapper.mapTupleValue(any(StringProperty.class),
        any(ValueContext.class))).thenReturn(DBEERPEDIA.BROUWTOREN_NAME.stringValue());

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, instanceOf(ImmutableMap.class));
    Map<String, Object> map = (Map<String, Object>) mappedEntity;
    assertThat(map.values(), hasSize(1));
    assertThat(map.get("name"), equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void map_MapsToResponseProperty_ForSingleResult() {
    // Arrange
    QueryBindingSet bindingSet = new QueryBindingSet();
    bindingSet.addBinding("name", DBEERPEDIA.BROUWTOREN_NAME);

    when(result.hasNext()).thenReturn(true, false);
    when(result.next()).thenReturn(bindingSet);

    StringProperty stringProperty = new StringProperty();
    when(schemaMapper.mapTupleValue(any(StringProperty.class),
            any(ValueContext.class))).thenReturn(DBEERPEDIA.BROUWTOREN_NAME.stringValue());

    TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ResponseProperty(new Response().schema(new ObjectProperty().properties(
            ImmutableMap.of("name", stringProperty.required(false)))))),
        result);

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, instanceOf(Map.class));

    Map<String, Object> map = (Map<String, Object>) mappedEntity;

    assertThat(map.values(), hasSize(1));
    assertThat(map.get("name"), equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void map_ForObjectPropertyOnlyMapsFirstResult_ForMultipleResults() {
    // Arrange
    StringProperty stringProperty = new StringProperty();
    final TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ObjectProperty().properties(ImmutableMap.of("name", stringProperty.required(false)))),
        result);
    when(result.hasNext()).thenReturn(true, true, false);
    QueryBindingSet bindingSet = new QueryBindingSet();
    bindingSet.addBinding("name", DBEERPEDIA.BROUWTOREN_NAME);
    when(result.next()).thenReturn(bindingSet, bindingSet);
    when(schemaMapper.mapTupleValue(any(StringProperty.class),
        any(ValueContext.class))).thenReturn("firstName").thenReturn("secondName");

    // Act
    Object mappedEntity = tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(mappedEntity, instanceOf(ImmutableMap.class));
    Map<String, Object> map = (Map<String, Object>) mappedEntity;
    assertThat(map.values(), hasSize(1));
    assertThat(map.get("name"), equalTo("firstName"));
  }

  @Test
  public void map_ThrowsException_ForSingleResultWithSingleObject() {
    // Arrange
    StringProperty stringProperty = new StringProperty();
    final TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ObjectProperty().properties(ImmutableMap.of("name", stringProperty.required(false)))),
        result);
    when(result.hasNext()).thenReturn(false);

    // Assert
    thrown.expect(EntityMapperRuntimeException.class);
    thrown.expectMessage("TupleQueryResult did not yield any values.");

    // Act
    tupleEntityMapper.map(entity, MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void map_ThrowException_ForRequiredPropertyWithAbsentBinding() {
    // Assert
    final TupleEntity entity = new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
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
