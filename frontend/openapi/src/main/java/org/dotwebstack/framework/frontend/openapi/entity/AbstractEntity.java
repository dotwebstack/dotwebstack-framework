package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;

abstract class AbstractEntity implements Entity {

  private Map<MediaType, Property> schemaMap;

  AbstractEntity(@NonNull Map<MediaType, Property> schemaMap) {
    this.schemaMap = schemaMap;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }
}
