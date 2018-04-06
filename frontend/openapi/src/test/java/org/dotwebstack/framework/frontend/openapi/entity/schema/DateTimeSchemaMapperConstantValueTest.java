package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.DateTimeProperty;
import java.time.LocalDateTime;
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
public class DateTimeSchemaMapperConstantValueTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final String CONSTANT_VALUE = OpenApiSpecificationExtensions.CONSTANT_VALUE;
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  private static final String EXPECTED_LOCAL_DATE_TIME = "1982-11-25T10:10:10";

  @Mock
  private GraphEntity entityMock;

  private SchemaMapperAdapter mapperAdapter;
  private DateTimeProperty property;
  private DateTimeSchemaMapper schemaMapper;
  private ValueContext valueContext;

  @Before
  public void setUp() {
    schemaMapper = new DateTimeSchemaMapper();
    property = new DateTimeProperty();
    mapperAdapter = new SchemaMapperAdapter(Collections.singletonList(schemaMapper));
    valueContext = ValueContext.builder().build();
  }

  @Test
  public void mapGraphValue_ReturnsLocalDateTime_WhenStringConstantValueIsDefined() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, EXPECTED_LOCAL_DATE_TIME));

    // Act
    LocalDateTime result = (LocalDateTime) mapperAdapter.mapGraphValue(property, entityMock,
        valueContext, mapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE_TIME));
  }

  @Test
  public void mapGraphValue_ReturnsLocalDateTime_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral(EXPECTED_LOCAL_DATE_TIME, XMLSchema.DATETIME);

    property.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, literal));

    // Act
    LocalDateTime result = (LocalDateTime) mapperAdapter.mapGraphValue(property, entityMock,
        valueContext, mapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE_TIME));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForNullConstantValue() {
    // Arrange
    property.setVendorExtensions(nullableMapOf(CONSTANT_VALUE));

    // Act
    Object result = mapperAdapter.mapGraphValue(property, entityMock, valueContext, mapperAdapter);

    // Assert
    assertThat(result, is(nullValue()));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForNullConstantAndRequiredProperty() {
    // Arrange
    property.setVendorExtensions(nullableMapOf(CONSTANT_VALUE));
    property.setRequired(true);

    // Assert
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage("x-dotwebstack-constant-value");
    expectedException.expectMessage("is null");
    expectedException.expectMessage("required");

    // Act
    schemaMapper.mapGraphValue(property, entityMock, valueContext, mapperAdapter);
  }

  private static Map<String, Object> nullableMapOf(String key) {
    Map<String, Object> result = new HashMap<>();

    result.put(key, null);

    return result;
  }

}
