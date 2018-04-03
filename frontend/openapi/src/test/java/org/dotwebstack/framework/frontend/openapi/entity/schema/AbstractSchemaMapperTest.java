package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.junit.Test;

public class AbstractSchemaMapperTest {

  @Test
  public void hasVendorExtension_ReturnsTrue_WhenPropertyHasExtension() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtension(schema,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void hasVendorExtension_ReturnsFalse_WhenPropertyHasExtension() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result =
        AbstractSchemaMapper.hasVendorExtension(schema, OpenApiSpecificationExtensions.LDPATH);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsTrue_WhenPropertyHasExtensionWithValue() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(schema,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsFalse_WhenPropertyHasExtensionWithoutValue() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(schema,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, false);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsFalse_WhenPropertyDoesNotHaveExtension() {
    // Arrange
    StringProperty schema = new StringProperty();

    schema.setVendorExtension(OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL,
        true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(schema,
        OpenApiSpecificationExtensions.LDPATH, false);

    // Assert
    assertThat(result, is(false));
  }

}
