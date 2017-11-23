package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSchemaMapperTest extends AbstractStringPropertyHandlerTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private StringSchemaMapper schemaMapper;

  private StringProperty schema;

  @Before
  public void setUp() {
    schemaMapper = new StringSchemaMapper();
    schema = new StringProperty();
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaMapper.mapTupleValue(null, DBEERPEDIA.BROUWTOREN_NAME);
  }

  @Test
  public void mapTupleValue_ThrowsException_WithMissingValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaMapper.mapTupleValue(schema, null);
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    String result = schemaMapper.mapTupleValue(schema, DBEERPEDIA.BROUWTOREN_NAME);

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void supports_ThrowsException_WithMissingSchema() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    schemaMapper.supports(null);
  }

  @Test
  public void supports_ReturnsTrue_ForStringSchema() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(schema);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonStringSchema() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new IntegerProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }


  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(stringProperty), Matchers.equalTo(true));
  }

  @Test
  public void handleNoVendorExtensions() {
    Object result =
        registry.mapGraphValue(stringProperty, entityBuilderContextMock, registry, contextMock);

    assertThat(result, nullValue());
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndRelativeLinkThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "",
        OpenApiSpecificationExtensions.RELATIVE_LINK, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock, registry, contextMock);
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock, registry, contextMock);
  }

  @Test
  public void handleMultipleVendorExtensionsRelativeLinkAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock, registry, contextMock);
  }

  @Test
  public void handleThreeVendorExtensionsThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OpenApiSpecificationExtensions.LDPATH, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContextMock, registry, contextMock);
  }

  private void expectExceptionAboutMultipleVendorExtensions() {
    expectedException.expect(SchemaMapperRuntimeException.class);
    expectedException.expectMessage(String.format(
        "A string object must have either no, a '%s', '%s' or '%s' property. "
            + "A string object cannot have a combination of these.",
        OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
        OpenApiSpecificationExtensions.CONSTANT_VALUE));
  }

}
