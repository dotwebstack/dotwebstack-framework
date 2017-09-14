package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.util.Map;
import java.util.Objects;

public final class Entity {

  private Object properties;

  private ImmutableMap<String, Property> schemaMap;

  public Entity(Object properties, Map<String, Property> schemaMap) {
    this.properties = Objects.requireNonNull(properties);
    this.schemaMap = Objects.requireNonNull(ImmutableMap.copyOf(schemaMap));
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
