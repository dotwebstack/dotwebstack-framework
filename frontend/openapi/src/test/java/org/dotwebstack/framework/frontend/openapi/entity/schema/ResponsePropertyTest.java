package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Response;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import org.junit.Test;

public class ResponsePropertyTest {

  @Test
  public void getDescription_ReturnsResponseDescription_ForInputResponse() {
    // Arrange
    Property schema = new ResponseProperty(new Response().description("foo"));
    schema.setDescription("bar");

    // Act
    String result = schema.getDescription();

    // Assert
    assertThat(result, is("foo"));
  }

  @Test
  public void getRequired_ReturnsResponseRequired_ForInputResponse() {
    // Arrange
    ObjectProperty schema = new ObjectProperty().required(true);
    ResponseProperty property = new ResponseProperty(new Response().schema(schema));

    property.setRequired(false);

    // Act
    boolean result = property.getRequired();

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void getSchema_ReturnsResponseSchema_ForInputResponse() {
    // Arrange
    ObjectProperty schema = new ObjectProperty();
    ResponseProperty property = new ResponseProperty(new Response().schema(schema));

    // Act
    Property result = property.getSchema();

    // Assert
    assertThat(result, sameInstance(schema));
  }

  @Test
  public void getVendorExtensions_ReturnsResponseVendorExtensions_ForInputResponse() {
    // Arrange
    ResponseProperty schema = new ResponseProperty(
        new Response().vendorExtension("x-foo", "bar").vendorExtension("x-baz", "qux"));
    schema.setVendorExtensions(ImmutableMap.of("x-quux", "gorge"));

    // Act
    Map<String, Object> result = schema.getVendorExtensions();

    // Assert
    assertThat(result, is(ImmutableMap.of("x-foo", "bar", "x-baz", "qux")));
  }

}
