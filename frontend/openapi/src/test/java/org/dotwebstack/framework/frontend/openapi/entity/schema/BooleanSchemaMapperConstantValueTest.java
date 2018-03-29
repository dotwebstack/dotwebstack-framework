package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.github.andrewoma.dexx.collection.Maps;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.BooleanProperty;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
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
public class BooleanSchemaMapperConstantValueTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final String CONSTANT_VALUE = OpenApiSpecificationExtensions.CONSTANT_VALUE;
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Mock
  private GraphEntity entityMock;

  private SchemaMapperAdapter mapperAdapter;
  private BooleanProperty property;
  private BooleanSchemaMapper mapper;
  private ValueContext valueContext;

  @Before
  public void setUp() {
    mapper = new BooleanSchemaMapper();
    property = new BooleanProperty();
    mapperAdapter = new SchemaMapperAdapter(Collections.singletonList(mapper));
    valueContext = ValueContext.builder().build();
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_WhenStringConstantValueIsDefined() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, "true"));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertBooleanTrue(result);
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_WhenBooleanConstantValueIsDefined() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, true));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertBooleanTrue(result);
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral("true", XMLSchema.BOOLEAN);

    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, literal));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertBooleanTrue(result);
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullConstantValue() {
    // Arrange
    property.setVendorExtensions(Maps.of(CONSTANT_VALUE, null).asMap());

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertNull(result);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullConstantAndRequiredProperty() {
    // Arrange
    property.setVendorExtensions(Maps.of(CONSTANT_VALUE, null).asMap());
    property.setRequired(true);

    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "BooleanProperty has 'x-dotwebstack-constant-value' vendor extension that is null, "
            + "but the property is required");

    // Act
    mapper.mapGraphValue(property, entityMock, valueContext, mapperAdapter);
  }

  private static void assertBooleanTrue(Object result) {
    assertThat(result, instanceOf(Boolean.class));
    assertThat(result, is(true));
  }

}
