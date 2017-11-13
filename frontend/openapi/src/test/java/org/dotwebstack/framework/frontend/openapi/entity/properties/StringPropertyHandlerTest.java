package org.dotwebstack.framework.frontend.openapi.entity.properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.junit.Test;

public class StringPropertyHandlerTest extends AbstractStringPropertyHandlerTest {

  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(stringProperty), equalTo(true));
  }

  @Test
  public void handleNoVendorExtensions() {
    Object result = registry.handle(stringProperty, entityBuilderContext, context);

    assertThat(result, nullValue());
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndRelativeLinkThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OasVendorExtensions.LDPATH,
        "", OasVendorExtensions.RELATIVE_LINK, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.handle(stringProperty, entityBuilderContext, context);
  }

  @Test
  public void handleMultipleVendorExtensionsLdPathQueryAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OasVendorExtensions.LDPATH,
        ImmutableMap.of(), OasVendorExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.handle(stringProperty, entityBuilderContext, context);
  }

  @Test
  public void handleMultipleVendorExtensionsRelativeLinkAndConstValueThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OasVendorExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OasVendorExtensions.CONSTANT_VALUE, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.handle(stringProperty, entityBuilderContext, context);
  }

  @Test
  public void handleThreeVendorExtensionsThrowsException() {
    stringProperty.setVendorExtensions(ImmutableMap.of(OasVendorExtensions.RELATIVE_LINK,
        ImmutableMap.of(), OasVendorExtensions.CONSTANT_VALUE, ImmutableMap.of(),
        OasVendorExtensions.LDPATH, ImmutableMap.of()));

    expectExceptionAboutMultipleVendorExtensions();

    registry.handle(stringProperty, entityBuilderContext, context);
  }

  private void expectExceptionAboutMultipleVendorExtensions() {
    expectedException.expect(PropertyHandlerRuntimeException.class);
    expectedException.expectMessage(String.format(
        "A string object must have either no, a '%s', '%s' or '%s' property. "
            + "A string object cannot have a combination of these.",
        OasVendorExtensions.LDPATH, OasVendorExtensions.RELATIVE_LINK,
        OasVendorExtensions.CONSTANT_VALUE));
  }

}
