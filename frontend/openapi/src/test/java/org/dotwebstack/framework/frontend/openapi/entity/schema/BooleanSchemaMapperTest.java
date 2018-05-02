package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.CONSTANT_VALUE;
import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.LDPATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
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
public class BooleanSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private TupleEntity tupleEntityMock;

  private BooleanSchemaMapper booleanSchemaMapper;
  private BooleanProperty booleanProperty;
  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    booleanSchemaMapper = new BooleanSchemaMapper();
    booleanProperty = new BooleanProperty();
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(booleanSchemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    booleanSchemaMapper.mapTupleValue(booleanProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnsValue_ForLiterals() {
    // Arrange & Act
    Boolean result = booleanSchemaMapper.mapTupleValue(booleanProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER).build());

    // Assert
    assertThat(result, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForBooleanProperty() {
    // Arrange & Act
    Boolean supported = booleanSchemaMapper.supports(booleanProperty);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonBooleanProperty() {
    // Arrange & Act
    Boolean supported = booleanSchemaMapper.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ThrowsEx_MultipleVendorExtensions() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("BooleanProperty ");
    thrown.expectMessage(CONSTANT_VALUE);
    thrown.expectMessage(LDPATH);

    // Arrange
    booleanProperty.vendorExtension(OpenApiSpecificationExtensions.LDPATH, "ld-Path");
    booleanProperty.vendorExtension(CONSTANT_VALUE, "true");
    ValueContext valueContext = ValueContext.builder().build();

    // Act
    booleanSchemaMapper.mapGraphValue(booleanProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);
  }

}
