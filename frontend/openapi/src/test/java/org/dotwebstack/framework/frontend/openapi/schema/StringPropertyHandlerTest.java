package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.junit.Test;

public class StringPropertyHandlerTest extends AbstractStringPropertyHandlerTest {

  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(stringProperty), equalTo(true));
  }

  @Test
  public void handleNoVendorExtensions() {
    Object result = registry.mapGraphValue(stringProperty, entityBuilderContext, registry, context);

    assertThat(result, nullValue());
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndRelativeLinkThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "",
        OpenApiSpecificationExtensions.RELATIVE_LINK, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContext, registry, context);
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContext, registry, context);
  }

  @Test
  public void handleMultipleVendorExtensionsRelativeLinkAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContext, registry, context);
  }

  @Test
  public void handleThreeVendorExtensionsThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OpenApiSpecificationExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OpenApiSpecificationExtensions.LDPATH, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.mapGraphValue(stringProperty, entityBuilderContext, registry, context);
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
