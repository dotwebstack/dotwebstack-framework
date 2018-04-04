package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.DateProperty;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class DateSchemaMapperConstantValueTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final String CONSTANT_VALUE = OpenApiSpecificationExtensions.CONSTANT_VALUE;
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Mock
  private GraphEntity entityMock;

  private SchemaMapperAdapter mapperAdapter;
  private DateProperty property;
  private DateSchemaMapper schemaMapper;
  private ValueContext valueContext;

  @Before
  public void setUp() {
    schemaMapper = new DateSchemaMapper();
    property = new DateProperty();
    mapperAdapter = new SchemaMapperAdapter(Collections.singletonList(schemaMapper));
    valueContext = ValueContext.builder().build();
  }

  @Test
  public void mapGraphValue_ReturnsDateValue_WhenStringConstantValueIsDefined() {
    // Arrange
    property.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.CONSTANT_VALUE, "1982-11-25"));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertThat(result, instanceOf(LocalDate.class));
    assertThat(result.toString(), is("1982-11-25"));
  }

  @Test
  public void mapGraphValue_ReturnsDateValue_WhenBooleanConstantValueIsDefined() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, "1982-11-25"));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertThat(result, instanceOf(LocalDate.class));
    assertThat(result.toString(), is("1982-11-25"));
  }

  @Test
  public void mapGraphValue_ReturnsDateValue_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral("1982-11-25", XMLSchema.DATE);

    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, literal));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertThat(result, instanceOf(LocalDate.class));
    assertThat(result.toString(), is("1982-11-25"));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullConstantValue() {
    // Arrange
    property.setVendorExtensions(nullableMapOf(CONSTANT_VALUE));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertNull(result);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullConstantAndRequiredProperty() {
    // Arrange
    property.setVendorExtensions(nullableMapOf(CONSTANT_VALUE));
    property.setRequired(true);

    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(
        "String Property has 'x-dotwebstack-constant-value' vendor extension that is null, "
            + "but the property is required.");

    // Act
    schemaMapper.mapGraphValue(property, entityMock, valueContext, mapperAdapter);
  }

  private static Map<String, Object> nullableMapOf(String key) {
    Map<String, Object> result = new HashMap<>();

    result.put(key, null);

    return result;
  }


}
