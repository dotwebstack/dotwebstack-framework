package org.dotwebstack.framework.frontend.openapi;

import io.swagger.models.properties.Property;

public interface EntityBuilder<T> {

  public Object build(T result, Property schema);

}
