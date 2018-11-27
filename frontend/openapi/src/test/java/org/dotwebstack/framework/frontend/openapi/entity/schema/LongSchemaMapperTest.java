package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LongSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private TupleEntity tupleEntityMock;

  private LongSchemaMapper longSchemaMapper;
  private IntegerSchema longProperty;

  @Before
  public void setUp() {
    longSchemaMapper = new LongSchemaMapper();
    longProperty = new IntegerSchema().format("int64");
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    longSchemaMapper.mapTupleValue(longProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Long result = longSchemaMapper.mapTupleValue(longProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_LITERS_PER_YEAR).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_LITERS_PER_YEAR.longValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForLongProperty() {
    // Arrange & Act
    Boolean supported = longSchemaMapper.supports(longProperty);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonIntegerProperty() {
    // Arrange & Act
    Boolean supported = longSchemaMapper.supports(new StringSchema());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void supports_ReturnsFalse_ForIntegerProperty() {
    // Arrange
    IntegerSchema integerSchema = new IntegerSchema();

    // Act
    Boolean supported = longSchemaMapper.supports(integerSchema);

    // Assert
    assertThat(supported, equalTo(false));
  }
}
