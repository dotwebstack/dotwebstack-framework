package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Arrays;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Value;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSchemaMapperTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private Value contextMock;

  private GraphEntityContext entityBuilderContextMock;

  private SchemaMapperAdapter schemaMapperAdapter;

  private StringSchemaMapper schemaMapper;
  private StringProperty stringProperty;

  @Before
  public void setUp() {
    entityBuilderContextMock = mock(GraphEntityContext.class);
    schemaMapper = new StringSchemaMapper();
    stringProperty = new StringProperty();
    schemaMapperAdapter = new SchemaMapperAdapter(Arrays.asList(schemaMapper));
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    String result = schemaMapper.mapTupleValue(stringProperty, DBEERPEDIA.BROUWTOREN_NAME);

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_NAME.stringValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForStringProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(stringProperty);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonStringProperty() {
    // Arrange & Act
    Boolean supported = schemaMapper.supports(new IntegerProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenNoVendorExtensionHasBeenDefined() {
    // Act
    Object result = schemaMapperAdapter.mapGraphValue(stringProperty, entityBuilderContextMock,
        schemaMapperAdapter, contextMock);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothLdPathAndRelativeLinkVendorExtensionsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "",
        OpenApiSpecificationExtensions.RELATIVE_LINK, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringProperty, entityBuilderContextMock, schemaMapperAdapter,
        contextMock);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothLdPathAndConstantValueVendorExtensionsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringProperty, entityBuilderContextMock, schemaMapperAdapter,
        contextMock);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenBothConstantValueAndRelativeLinkVendorExtsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringProperty, entityBuilderContextMock, schemaMapperAdapter,
        contextMock);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenConstantValueLdPathAndRelativeLinkVendorExtsAreDefined() {
    // Assert
    expectExceptionAboutMultipleVendorExtensions();

    // Arrange
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OpenApiSpecificationExtensions.LDPATH, ImmutableMap.of()));

    // Act
    schemaMapperAdapter.mapGraphValue(stringProperty, entityBuilderContextMock, schemaMapperAdapter,
        contextMock);
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
