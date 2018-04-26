package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RefSchemaMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_REF = "#/graphEntityMock/fooModel";
  private static final String KEY_1 = "one";
  private static final String KEY_2 = "_two";
  private static final String KEY_3 = "three";

  private static final StringProperty PROPERTY_1 = new StringProperty();
  private static final IntegerProperty PROPERTY_2 = new IntegerProperty();
  private static final StringProperty PROPERTY_3 = new StringProperty();
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral("CONSTANT");
  private static final Literal VALUE_2 =
      SimpleValueFactory.getInstance().createLiteral("123", XMLSchema.INTEGER);
  private static final String LD_PATH_QUERY = ".";

  static {
    PROPERTY_1.getVendorExtensions().put(OpenApiSpecificationExtensions.CONSTANT_VALUE, VALUE_1);
    PROPERTY_2.getVendorExtensions().put(OpenApiSpecificationExtensions.LDPATH, LD_PATH_QUERY);
    PROPERTY_3.getVendorExtensions().put(OpenApiSpecificationExtensions.CONSTANT_VALUE, null);
  }

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private final RefSchemaMapper refSchemaMapper;
  private RefProperty refProperty;
  private SchemaMapperAdapter schemaMapperAdapter;

  public RefSchemaMapperTest() {
    List<SchemaMapper<? extends Property, ?>> mappers = new ArrayList<>();
    refSchemaMapper = new RefSchemaMapper();
    mappers.add(refSchemaMapper);
    mappers.add(new IntegerSchemaMapper());
    mappers.add(new StringSchemaMapper());
    schemaMapperAdapter = new SchemaMapperAdapter(mappers);
  }

  @Before
  public void setUp() {
    refProperty = new RefProperty();
  }

  @Test
  public void supports_ReturnsTrue_ForRefProperty() {
    // Act
    boolean result = refSchemaMapper.supports(refProperty);

    // Assert
    assertTrue(result);
  }

  @Test
  public void mapGraphValue_ThrowsException_WhenRefCannotBeResolved() {
    // Arrange
    refProperty.set$ref(DUMMY_REF);
    when(graphEntityMock.getSwaggerDefinitions()).thenReturn(ImmutableMap.of());

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("Unable to resolve reference to swagger model: '%s'.",
        refProperty.getSimpleRef()));

    // Act
    refSchemaMapper.mapGraphValue(refProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ReturnsResults_WhenRefCanBeResolved() {
    // Arrange
    refProperty.set$ref(DUMMY_REF);
    Model refModel = new ModelImpl();
    refModel.setProperties(ImmutableMap.of(KEY_1, PROPERTY_1, KEY_2, PROPERTY_2));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
    when(graphEntityMock.getSwaggerDefinitions()).thenReturn(
        ImmutableMap.of(refProperty.getSimpleRef(), refModel));
    when(ldPathExecutorMock.ldPathQuery(valueMock, LD_PATH_QUERY)).thenReturn(
        ImmutableList.of(VALUE_2));

    // Act
    Map<String, Object> result = (Map<String, Object>) refSchemaMapper.mapGraphValue(refProperty,
        graphEntityMock, ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.keySet(), hasSize(2));
    assertEquals(((Optional) result.get(KEY_1)).orNull(), VALUE_1.stringValue());
    assertEquals(((Optional) result.get(KEY_2)).orNull(), VALUE_2.intValue());
  }

  @Test
  public void mapGraphValue_ReturnsResults_WhenRefCanBeResolvedAndOneIsNull() {
    // Arrange
    refProperty.set$ref(DUMMY_REF);
    Model refModel = new ModelImpl();
    refModel.getVendorExtensions().put(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);
    refModel.setProperties(
        ImmutableMap.of(KEY_1, PROPERTY_1, KEY_2, PROPERTY_2, KEY_3, PROPERTY_3));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
    when(graphEntityMock.getSwaggerDefinitions()).thenReturn(
        ImmutableMap.of(refProperty.getSimpleRef(), refModel));
    when(ldPathExecutorMock.ldPathQuery(valueMock, LD_PATH_QUERY)).thenReturn(
        ImmutableList.of(VALUE_2));

    // Act
    Map<String, Optional> result =
        (Map<String, Optional>) refSchemaMapper.mapGraphValue(refProperty, graphEntityMock,
            ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.keySet(), hasSize(2));
    assertEquals(result.get(KEY_1).orNull(), VALUE_1.stringValue());
    assertEquals(result.get(KEY_2).orNull(), VALUE_2.intValue());
    assertThat(result.containsKey(KEY_3), is(false));
  }
}
