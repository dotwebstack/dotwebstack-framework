package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.CONSTANT_VALUE;
import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.LDPATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class BooleanSchemaMapperTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private BooleanSchemaMapper schemaMapper;

  private BooleanProperty property;

  @Mock
  private GraphEntity entitymock;
  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    schemaMapper = new BooleanSchemaMapper();
    property = new BooleanProperty();
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(schemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    exception.expect(SchemaMapperRuntimeException.class);
    exception.expectMessage("Value is not a literal value.");

    // Arrange & Act
    schemaMapper.mapTupleValue(property, ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Boolean result = schemaMapper.mapTupleValue(property,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER).build());

    // Assert
    assertThat(result, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForBooleanProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(property);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonBooleanProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ThrowsEx_MultipleVendorExtensions() {
    // Arrange
    property.vendorExtension(OpenApiSpecificationExtensions.LDPATH, "ld-Path");
    property.vendorExtension(CONSTANT_VALUE, "true");
    ValueContext context = ValueContext.builder().build();

    // Assert
    exception.expect(SchemaMapperRuntimeException.class);
    exception.expectMessage("BooleanProperty ");
    exception.expectMessage(CONSTANT_VALUE);
    exception.expectMessage(LDPATH);

    // Act
    schemaMapper.mapGraphValue(property, entitymock, context, schemaMapperAdapter);
  }

}
