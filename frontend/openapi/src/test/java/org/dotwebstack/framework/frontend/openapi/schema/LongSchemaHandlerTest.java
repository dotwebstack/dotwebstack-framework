package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.StringProperty;
import java.math.BigInteger;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LongSchemaHandlerTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private LongSchemaHandler schemaHandler;

  private LongProperty schema;

  @Before
  public void setUp() {
    schemaHandler = new LongSchemaHandler();
    schema = new LongProperty();
  }

  @Test
  public void handleTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.handleTupleValue(null, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION);
  }

  @Test
  public void handleTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.handleTupleValue(schema, null);
  }

  @Test
  public void handleTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaHandlerRuntimeException.class);
    thrown.expectMessage(String.format("Schema '%s' is not a literal value.", schema.getName()));

    // Arrange & Act
    schemaHandler.handleTupleValue(schema, DBEERPEDIA.BROUWTOREN);
  }

  @Test
  public void handleTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    BigInteger result =
        schemaHandler.handleTupleValue(schema, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION);

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.integerValue()));
  }

  @Test
  public void supports_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.supports(null);
  }

  @Test
  public void supports_ReturnsTrue_ForLongSchema() {
    // Arrange & Act
    Boolean supported = schemaHandler.supports(schema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonLongSchema() {
    // Arrange & Act
    Boolean supported = schemaHandler.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

}
