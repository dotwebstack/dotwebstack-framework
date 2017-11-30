package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.StringProperty;
import java.util.ArrayList;
import java.util.List;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

// XXX (PvH) Nette test, maar veel Sonar pleasing. Ik dacht dat we hadden afgesproken dit niet
// (meer) te doen :-)
@RunWith(MockitoJUnitRunner.class)
public class DoubleSchemaMapperTest {

  @Mock
  private GraphEntityContext graphEntityContext;

  // XXX (PvH) Qua volgorde zou ik public member voor privates zetten
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private DoubleSchemaMapper schemaMapper;

  private DoubleProperty schema;

  private SchemaMapperAdapter schemaAdapter;

  @Before
  public void setUp() {
    schemaMapper = new DoubleSchemaMapper();
    schema = new DoubleProperty();
    List schemaMappers = new ArrayList<>();
    schemaMappers.add(schemaMapper);
    schemaAdapter = new SchemaMapperAdapter(schemaMappers);
  }

  @Test
  public void mapGraph_ThrowsException_WithUnsupportedOperation() {
    // Assert
    thrown.expect(UnsupportedOperationException.class);

    // Arrange & Act
    schemaMapper.mapGraphValue(null, graphEntityContext, schemaAdapter, null);
  }


  @Test
  public void mapTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaMapper.mapTupleValue(null, DBEERPEDIA.BROUWTOREN_LITERS_PER_YEAR);
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaMapper.mapTupleValue(schema, null);
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    schemaMapper.mapTupleValue(schema, DBEERPEDIA.BROUWTOREN);
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Double result = schemaMapper.mapTupleValue(schema, DBEERPEDIA.BROUWTOREN_FTE);

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_FTE.doubleValue()));
  }

  @Test
  public void supports_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaMapper.supports(null);
  }

  @Test
  public void supports_ReturnsTrue_ForDoubleSchema() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(schema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  // XXX (PvH) supports_ReturnsFalse_ForNonDoubleSchema
  @Test
  public void supports_ReturnsTrue_ForNonDoubleSchema() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

}
