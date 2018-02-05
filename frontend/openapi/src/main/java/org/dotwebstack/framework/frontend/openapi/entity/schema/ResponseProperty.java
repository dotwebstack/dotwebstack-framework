package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.Response;
import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import lombok.NonNull;

@java.lang.SuppressWarnings("squid:S2160")
public final class ResponseProperty extends AbstractProperty implements Property {

  private final Response response;

  public ResponseProperty(@NonNull Response response) {
    this.response = response;
  }

  public Response getResponse() {
    return response;
  }

  /**
   * @return The input {@link Response#getDescription()}.
   */
  @Override
  public String getDescription() {
    return response.getDescription();
  }

  /**
   * @return The input {@link Response#getSchema()}.
   */
  public Property getSchema() {
    return response.getSchema();
  }

  /**
   * @return The required property on the input {@link Response#getSchema()}.
   */
  @Override
  public boolean getRequired() {
    return getSchema().getRequired();
  }

  /**
   * @return The input {@link Response#getVendorExtensions()}.
   */
  @Override
  public Map<String, Object> getVendorExtensions() {
    return response.getVendorExtensions();
  }

}
