package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.entity.SchemaMapperContextImpl;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaMapperAdapterTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SchemaMapper<StringProperty, String> stringSchemaMapper;

  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    schemaMapperAdapter = new SchemaMapperAdapter(ImmutableList.of(stringSchemaMapper));
  }

  @Test
  public void constructor_ThrowsException_WithMissingSchemaHandlers() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SchemaMapperAdapter(null);
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    schemaMapperAdapter.mapTupleValue(null,
        SchemaMapperContextImpl.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    schemaMapperAdapter.mapTupleValue(new StringProperty(),
        SchemaMapperContextImpl.builder().value(null).build());
  }

  @Test
  public void mapTupleValue_ThrowsException_WhenNoSupportingHandlerFound() {
    // Arrange
    IntegerProperty schema = new IntegerProperty();
    when(stringSchemaMapper.supports(schema)).thenReturn(false);

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("No schema handler available for '%s'.", schema.getClass().getName()));

    // Act
    schemaMapperAdapter.mapTupleValue(schema,
        SchemaMapperContextImpl.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());
  }

  @Test
  public void mapTupleValue_ReturnsHandledValue_WhenSupportingHandlerFound() {
    // Arrange
    StringProperty schema = new StringProperty();
    String expectedValue = DBEERPEDIA.BROUWTOREN_NAME.stringValue();
    when(stringSchemaMapper.supports(schema)).thenReturn(true);
    when(stringSchemaMapper.mapTupleValue(schema,
        SchemaMapperContextImpl.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build())).thenReturn(
            expectedValue);

    // Act
    Object value = schemaMapperAdapter.mapTupleValue(schema,
        SchemaMapperContextImpl.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());

    // Assert
    assertThat(value, equalTo(expectedValue));
  }

}
