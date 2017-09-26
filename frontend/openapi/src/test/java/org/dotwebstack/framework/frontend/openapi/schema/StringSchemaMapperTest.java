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
public class StringSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private StringSchemaMapper schemaHandler;

  private StringProperty schema;

  @Before
  public void setUp() {
    schemaHandler = new StringSchemaMapper();
    schema = new StringProperty();
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.mapTupleValue(null, DBEERPEDIA.BROUWTOREN_NAME);
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.mapTupleValue(schema, null);
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    String result = schemaHandler.mapTupleValue(schema, DBEERPEDIA.BROUWTOREN_NAME);

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void supports_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaHandler.supports(null);
  }

  @Test
  public void supports_ReturnsTrue_ForStringSchema() {
    // Arrange & Act
    Boolean supported = schemaHandler.supports(schema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonStringSchema() {
    // Arrange & Act
    Boolean supported = schemaHandler.supports(new IntegerProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

}
