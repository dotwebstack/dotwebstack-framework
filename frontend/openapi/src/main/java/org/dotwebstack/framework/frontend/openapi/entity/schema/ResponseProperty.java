package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.Response;
import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import lombok.NonNull;

public final class ResponseProperty extends AbstractProperty implements Property {

  private final Response response;

  public ResponseProperty(@NonNull Response response) {
    this.response = response;
  }

  @Override
  public String getDescription() {
    return response.getDescription();
  }

  public Property getSchema() {
    return response.getSchema();
  }

  @Override
  public Map<String, Object> getVendorExtensions() {
    return response.getVendorExtensions();
  }

}
