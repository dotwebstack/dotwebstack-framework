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
    StringProperty property = new StringProperty();

    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtension(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void hasVendorExtension_ReturnsFalse_WhenPropertyHasExtension() {
    // Arrange
    StringProperty property = new StringProperty();

    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Act
    boolean result =
        AbstractSchemaMapper.hasVendorExtension(property, OpenApiSpecificationExtensions.LDPATH);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsTrue_WhenPropertyHasExtensionWithValue() {
    // Arrange
    StringProperty property = new StringProperty();

    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsFalse_WhenPropertyHasExtensionWithoutValue() {
    // Arrange
    StringProperty property = new StringProperty();

    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, false);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasVendorExtensionWithValue_ReturnsFalse_WhenPropertyDoesNotHaveExtension() {
    // Arrange
    StringProperty property = new StringProperty();

    property.setVendorExtension(
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

    // Act
    boolean result = AbstractSchemaMapper.hasVendorExtensionWithValue(property,
        OpenApiSpecificationExtensions.LDPATH, false);

    // Assert
    assertThat(result, is(false));
  }

}
