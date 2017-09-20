package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.util.Map;
import lombok.NonNull;

public final class Entity {

  private Object properties;

  private ImmutableMap<String, Property> schemaMap;

  public Entity(@NonNull Object properties, @NonNull Map<String, Property> schemaMap) {
    this.properties = properties;
    this.schemaMap = ImmutableMap.copyOf(schemaMap);
  }

  public Object getProperties() {
    return properties;
  }

  public Map<String, Property> getSchemaMap() {
    return schemaMap;
  }

  public Property getSchema(String mediaType) {
    if (!schemaMap.containsKey(mediaType)) {
      throw new NullPointerException();
    }

    return schemaMap.get(mediaType);
  }

}
