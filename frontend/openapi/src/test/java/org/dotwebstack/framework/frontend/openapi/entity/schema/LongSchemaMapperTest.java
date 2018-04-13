package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.StringProperty;
import java.math.BigInteger;
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
  private LongProperty longProperty;

  @Before
  public void setUp() {
    longSchemaMapper = new LongSchemaMapper();
    longProperty = new LongProperty();
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
  public void supports_ReturnsTrue_ForNonLongProperty() {
    // Arrange & Act
    Boolean supported = longSchemaMapper.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }
}
