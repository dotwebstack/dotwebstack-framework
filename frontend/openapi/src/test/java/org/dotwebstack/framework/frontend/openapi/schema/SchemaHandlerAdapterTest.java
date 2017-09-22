package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaHandlerAdapterTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SchemaHandler<StringProperty, String> stringSchemaHandler;

  private SchemaHandlerAdapter schemaHandlerAdapter;

  @Before
  public void setUp() {
    schemaHandlerAdapter = new SchemaHandlerAdapter(ImmutableList.of(stringSchemaHandler));
  }

  @Test
  public void constructor_ThrowsException_WithMissingSchemaHandlers() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SchemaHandlerAdapter(null);
  }

  @Test
  public void handleTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    schemaHandlerAdapter.handleTupleValue(null, DBEERPEDIA.BROUWTOREN_NAME);
  }

  @Test
  public void handleTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    schemaHandlerAdapter.handleTupleValue(new StringProperty(), null);
  }

  @Test
  public void handleTupleValue_ThrowsException_WhenNoSupportingHandlerFound() {
    // Arrange
    IntegerProperty schema = new IntegerProperty();
    when(stringSchemaHandler.supports(schema)).thenReturn(false);

    // Assert
    thrown.expect(SchemaHandlerRuntimeException.class);
    thrown.expectMessage(
        String.format("No schema handler available for '%s'.", schema.getClass().getName()));

    // Act
    schemaHandlerAdapter.handleTupleValue(schema, DBEERPEDIA.BROUWTOREN_NAME);
  }

  @Test
  public void handleTupleValue_ReturnsHandledValue_WhenSupportingHandlerFound() {
    // Arrange
    StringProperty schema = new StringProperty();
    String expectedValue = DBEERPEDIA.BROUWTOREN_NAME.stringValue();
    when(stringSchemaHandler.supports(schema)).thenReturn(true);
    when(stringSchemaHandler.handleTupleValue(schema, DBEERPEDIA.BROUWTOREN_NAME)).thenReturn(
        expectedValue);

    // Act
    Object value = schemaHandlerAdapter.handleTupleValue(schema, DBEERPEDIA.BROUWTOREN_NAME);

    // Assert
    assertThat(value, equalTo(expectedValue));
  }

}
