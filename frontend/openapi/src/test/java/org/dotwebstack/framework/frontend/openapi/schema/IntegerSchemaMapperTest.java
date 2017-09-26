package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegerSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private IntegerSchemaMapper schemaHandler;

  private IntegerProperty schema;

  @Before
  public void setUp() {
    schemaHandler = new IntegerSchemaMapper();
    schema = new IntegerProperty();
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.mapTupleValue(null, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION);
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.mapTupleValue(schema, null);
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaHandlerRuntimeException.class);
    thrown.expectMessage(String.format("Schema '%s' is not a literal value.", schema.getName()));

    // Arrange & Act
    schemaHandler.mapTupleValue(schema, DBEERPEDIA.BROUWTOREN);
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Integer result = schemaHandler.mapTupleValue(schema, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION);

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.intValue()));
  }

  @Test
  public void supports_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.supports(null);
  }

  @Test
  public void supports_ReturnsTrue_ForIntegerSchema() {
    // Arrange & Act
    Boolean supported = schemaHandler.supports(schema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonIntegerSchema() {
    // Arrange & Act
    Boolean supported = schemaHandler.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

}
