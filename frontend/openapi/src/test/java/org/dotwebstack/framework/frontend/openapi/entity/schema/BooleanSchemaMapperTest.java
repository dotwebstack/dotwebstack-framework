package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.CONSTANT_VALUE;
import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.LDPATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.StringSchema;
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
  private BooleanSchema booleanSchema;
  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    booleanSchemaMapper = new BooleanSchemaMapper();
    booleanSchema = new BooleanSchema();
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(booleanSchemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    booleanSchemaMapper.mapTupleValue(booleanSchema, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnsValue_ForLiterals() {
    // Arrange & Act
    Boolean result = booleanSchemaMapper.mapTupleValue(booleanSchema, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER).build());

    // Assert
    assertThat(result, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForBooleanSchema() {
    // Arrange & Act
    Boolean supported = booleanSchemaMapper.supports(booleanSchema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonBooleanSchema() {
    // Arrange & Act
    Boolean supported = booleanSchemaMapper.supports(new StringSchema());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ThrowsEx_MultipleVendorExtensions() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("BooleanSchema ");
    thrown.expectMessage(CONSTANT_VALUE);
    thrown.expectMessage(LDPATH);

    // Arrange
    booleanSchema.addExtension(OpenApiSpecificationExtensions.LDPATH, "ld-Path");
    booleanSchema.addExtension(CONSTANT_VALUE, "true");
    ValueContext valueContext = ValueContext.builder().build();

    // Act
    booleanSchemaMapper.mapGraphValue(booleanSchema, false, graphEntityMock, valueContext,
        schemaMapperAdapter);
  }

}
